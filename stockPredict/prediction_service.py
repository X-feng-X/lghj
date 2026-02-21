from fastapi import FastAPI, HTTPException
from pydantic import BaseModel
import torch
import pandas as pd
import numpy as np
import requests
from datetime import datetime, timedelta
import os

# 导入本地模块
from LSTMModel import lstm
from parser_my import args

app = FastAPI()

# 模型设置
MODEL_PATH = args.save_file
DEVICE = args.device

# 初始化模型
model = lstm(input_size=args.input_size, hidden_size=args.hidden_size, num_layers=args.layers, output_size=1)
model.to(DEVICE)

# 加载权重（如果存在）
if os.path.exists(MODEL_PATH):
    checkpoint = torch.load(MODEL_PATH, map_location=DEVICE)
    model.load_state_dict(checkpoint['state_dict'])
    model.eval()
    print(f"从 {MODEL_PATH} 加载模型")
else:
    print(f"警告: 模型文件 {MODEL_PATH} 未找到。使用初始化权重。")


class PredictionResponse(BaseModel):
    symbol: str
    predictions: list


import akshare as ak


def fetch_akshare_data(symbol: str, days: int = 300):
    """
    从 AKShare API 获取历史数据。
    symbol: 例如 "sz000001" 或 "000001"
    """
    try:
        # 清理 symbol: "sz000001" -> "000001"
        code = symbol
        if symbol.startswith("sz") or symbol.startswith("sh"):
            code = symbol[2:]

        # 计算开始日期
        end_date = datetime.now().strftime("%Y%m%d")
        start_date = (datetime.now() - timedelta(days=days * 2)).strftime("%Y%m%d")  # 多获取一些以确保足够

        # 调用 AKShare
        # stock_zh_a_hist: code, period, start_date, end_date, adjust='qfq'
        df = ak.stock_zh_a_hist(symbol=code, period="daily", start_date=start_date, end_date=end_date, adjust="qfq")

        if df.empty:
            raise ValueError(f"未找到 {symbol} 的数据")

        # 重命名列以匹配模型期望
        # AKShare 列名: 日期, 股票代码, 开盘, 收盘, 最高, 最低, 成交量, 成交额, 振幅, 涨跌幅, 涨跌额, 换手率
        # 模型需要: close, open, high, low, change, pct_chg, vol, amount

        rename_map = {
            "日期": "date",
            "开盘": "open",
            "收盘": "close",
            "最高": "high",
            "最低": "low",
            "成交量": "vol",
            "成交额": "amount",
            "涨跌额": "change",
            "涨跌幅": "pct_chg"
        }
        df = df.rename(columns=rename_map)

        # 确保数据足够
        if len(df) > days:
            df = df.iloc[-days:]

        # 重新排序列
        cols = ['close', 'open', 'high', 'low', 'change', 'pct_chg', 'vol', 'amount']

        # 确保类型正确
        for col in cols:
            df[col] = pd.to_numeric(df[col], errors='coerce')

        return df[cols], df['date'].tolist()

    except Exception as e:
        print(f"从 AKShare 获取数据出错: {e}")
        return None, None


def normalize(df):
    """
    最小-最大归一化。
    返回 归一化后的df, 最小值, 最大值
    """
    min_vals = df.min()
    max_vals = df.max()
    norm_df = (df - min_vals) / (max_vals - min_vals)
    return norm_df, min_vals, max_vals


def denormalize(val, min_val, max_val):
    return val * (max_val - min_val) + min_val


@app.get("/api/user/stock/minute/{symbol}")
async def get_minute_data(symbol: str):
    """
    获取股票分时数据（最近一个交易日的分钟级数据）。
    """
    try:
        # 清理 symbol: "sz000001" -> "000001"
        code = symbol
        if symbol.startswith("sz") or symbol.startswith("sh"):
            code = symbol[2:]

        # 调用 AKShare 获取分时数据
        # stock_zh_a_minute: symbol='sh600751', period='1', adjust='qfq'
        # 注意：akshare 的 symbol 格式可能需要带前缀，或者不需要，具体取决于接口。
        # stock_zh_a_minute 接口通常需要带前缀 sh/sz

        ak_symbol = symbol
        if not (symbol.startswith("sz") or symbol.startswith("sh")):
            # 简单尝试推断：6开头是sh，0/3开头是sz
            if symbol.startswith("6"):
                ak_symbol = "sh" + symbol
            else:
                ak_symbol = "sz" + symbol

        df = ak.stock_zh_a_minute(symbol=ak_symbol, period="1", adjust="qfq")

        if df.empty:
            raise HTTPException(status_code=404, detail=f"未找到 {symbol} 的分时数据")

        # AKShare 分时数据列名: day, open, high, low, close, volume
        # 我们只需要时间、价格、成交量
        # 格式化为: [{"time": "0930", "price": 10.5, "volume": 1000, "date": "2023-10-27"}, ...]

        result = []

        # 找到最近的日期
        if df.empty:
            raise HTTPException(status_code=404, detail=f"未找到 {symbol} 的分时数据")

        # 确保 day 列是 datetime 类型
        df['day'] = pd.to_datetime(df['day'])

        # 获取最新的日期
        latest_date = df['day'].dt.date.max()

        # 筛选出最新日期的数据
        latest_df = df[df['day'].dt.date == latest_date]

        for _, row in latest_df.iterrows():
            dt_obj = row['day']

            item = {
                "time": dt_obj.strftime("%H%M"),
                "price": float(row['close']),
                "volume": int(float(row['volume'])),
                "date": dt_obj.strftime("%Y-%m-%d")  # 也就是当天的日期
            }
            result.append(item)

        return result

    except Exception as e:
        print(f"获取分时数据出错: {e}")
        raise HTTPException(status_code=500, detail=str(e))


@app.get("/api/user/stock/predict/{symbol}", response_model=PredictionResponse)
async def predict_stock(symbol: str):
    """
    预测未来 30 天的股票价格。
    """
    # 获取数据
    df, dates = fetch_akshare_data(symbol)

    if df is None or df.empty:
        raise HTTPException(status_code=404, detail=f"未找到 {symbol} 的数据")

    # 准备数据
    norm_df, min_vals, max_vals = normalize(df)

    # 序列长度
    seq_len = args.sequence_length  # 默认 5
    input_size = args.input_size  # 默认 8

    if len(norm_df) < seq_len:
        raise HTTPException(status_code=400, detail="数据不足以进行预测")

    # 初始输入: 最后 seq_len 天
    current_seq = norm_df.iloc[-seq_len:].values
    # 形状: (seq_len, input_size)

    predictions = []

    # Predict for next 30 days
    # 处理日期：如果 dates[-1] 是字符串，则解析；如果是 datetime.date/datetime 对象，则直接使用
    last_date_raw = dates[-1]
    if isinstance(last_date_raw, str):
        last_date = datetime.strptime(last_date_raw, "%Y-%m-%d")
    elif isinstance(last_date_raw, (datetime, pd.Timestamp)):
        last_date = last_date_raw
    elif hasattr(last_date_raw, 'strftime'):  # 兼容 datetime.date
        last_date = datetime.combine(last_date_raw, datetime.min.time())
    else:
        # 尝试转换为字符串再解析
        last_date = datetime.strptime(str(last_date_raw), "%Y-%m-%d")

    # 我们需要维护一个运行中的特征序列。
    # 为了预测未来几天，我们必须估计未来的特征（开盘价、最高价、最低价等）
    # 策略:
    # 1. 预测收盘价。
    # 2. 假设下一个开盘价 = 上一个收盘价（预测值）。
    # 3. 假设最高价 = max(开盘价, 收盘价)，最低价 = min(开盘价, 收盘价)。
    # 4. 假设成交量 = 过去 5 天的平均值。
    # 5. 基于这些计算涨跌额、涨跌幅、成交额。

    # 转换为 tensor
    current_seq_tensor = torch.FloatTensor(current_seq).unsqueeze(0).to(DEVICE)
    # 形状: (1, seq_len, input_size) 如果 batch_first=True

    model.eval()

    with torch.no_grad():
        for i in range(30):
            # 预测
            # 模型前向传播: out = model(x) -> (batch, output_size) 如果我们使用最后一步
            # 但是 LSTMModel 返回 (num_layers, batch, hidden) -> linear -> (num_layers, batch, output)
            # Train.py 逻辑: pred = pred[1, :, :] (取第二层输出)

            output = model(current_seq_tensor)
            # output 形状: (layers, batch, 1)
            # 我们取最后一层？train.py 取索引 1（第二层）。
            # 如果 layers=2（默认），索引 1 是最后一层。
            # 如果 layers=1，索引 1 越界！
            # 让我们检查 args.layers。默认为 2。

            if args.layers >= 2:
                pred_val = output[1, :, :].item()
            else:
                pred_val = output[-1, :, :].item()  # 回退到最后一层

            # 反归一化收盘价
            pred_price = denormalize(pred_val, min_vals['close'], max_vals['close'])

            # 生成下一个日期（简单跳过周末？或者只是 +1 天）
            next_date = last_date + timedelta(days=1)
            while next_date.weekday() > 4:  # 跳过周六/周日
                next_date += timedelta(days=1)
            last_date = next_date

            predictions.append({
                "date": next_date.strftime("%Y-%m-%d"),
                "price": round(pred_price, 2)
            })

            # 准备下一个输入步骤
            # 我们需要构建一个新的特征行
            # 特征: close, open, high, low, change, pct_chg, vol, amount

            prev_close_norm = current_seq[-1, 0]  # 归一化序列中的最后收盘价
            prev_close_real = denormalize(prev_close_norm, min_vals['close'], max_vals['close'])

            # 构建新行（归一化）
            new_row = np.zeros(input_size)

            # 1. 收盘价（预测值）
            new_row[0] = pred_val

            # 2. 开盘价（假设 = 上一个收盘价）
            # 使用开盘价的最小值/最大值归一化 prev_close_real
            new_open_norm = (prev_close_real - min_vals['open']) / (max_vals['open'] - min_vals['open'])
            new_row[1] = new_open_norm

            # 3. 最高价（假设 = max(开盘价, 收盘价)）
            high_real = max(prev_close_real, pred_price)
            new_row[2] = (high_real - min_vals['high']) / (max_vals['high'] - min_vals['high'])

            # 4. 最低价（假设 = min(开盘价, 收盘价)）
            low_real = min(prev_close_real, pred_price)
            new_row[3] = (low_real - min_vals['low']) / (max_vals['low'] - min_vals['low'])

            # 5. 涨跌额
            change_real = pred_price - prev_close_real
            new_row[4] = (change_real - min_vals['change']) / (max_vals['change'] - min_vals['change'])

            # 6. 涨跌幅
            pct_chg_real = (change_real / prev_close_real) * 100
            new_row[5] = (pct_chg_real - min_vals['pct_chg']) / (max_vals['pct_chg'] - min_vals['pct_chg'])

            # 7. 成交量（假设为过去 seq_len 天的平均值）
            # 反归一化最近的成交量
            recent_vols = [denormalize(x[6], min_vals['vol'], max_vals['vol']) for x in current_seq]
            avg_vol = sum(recent_vols) / len(recent_vols)
            new_row[6] = (avg_vol - min_vals['vol']) / (max_vals['vol'] - min_vals['vol'])

            # 8. 成交额
            amount_real = avg_vol * pred_price
            new_row[7] = (amount_real - min_vals['amount']) / (max_vals['amount'] - min_vals['amount'])

            # 更新序列
            # 移除第一步，添加新步骤
            current_seq = np.vstack([current_seq[1:], new_row])

            # 更新 tensor
            current_seq_tensor = torch.FloatTensor(current_seq).unsqueeze(0).to(DEVICE)

    return {"symbol": symbol, "predictions": predictions}


if __name__ == "__main__":
    import uvicorn

    uvicorn.run(app, host="0.0.0.0", port=8001)
