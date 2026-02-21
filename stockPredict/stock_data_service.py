from fastapi import FastAPI, HTTPException
import akshare as ak
import pandas as pd
from datetime import datetime, timedelta

app = FastAPI()


@app.get("/api/user/stock/data")
async def get_stock_data(symbol: str, period: str):
    """
    获取股票历史数据（日K/周K/月K）
    :param symbol: 股票代码
    :param period: D=日, W=周, M=月
    :return: 历史数据列表
    """
    try:
        # 清理 symbol: "sz000001" -> "000001"
        code = symbol
        if symbol.startswith("sz") or symbol.startswith("sh"):
            code = symbol[2:]

        # 转换 period 参数为 akshare 格式
        # akshare.stock_zh_a_hist period参数: "daily", "weekly", "monthly"
        ak_period = "daily"
        if period == "W":
            ak_period = "weekly"
        elif period == "M":
            ak_period = "monthly"
        elif period != "D":
            raise HTTPException(status_code=400, detail="Invalid period. Use D, W, or M.")

        # 获取数据
        # 默认获取最近10年的数据 (Updated to 10 years)
        end_date = datetime.now().strftime("%Y%m%d")
        start_date = (datetime.now() - timedelta(days=365 * 10)).strftime("%Y%m%d")

        # Check for Index symbols
        # Common Indices: sh000001 (Shanghai), sz399001 (Shenzhen), sz399006 (ChiNext)
        is_index = symbol in ["sh000001", "sz399001", "sz399006"]

        if is_index:
            # Use index API
            df = ak.stock_zh_index_daily(symbol=symbol)
            # Filter by date manually since API returns all history usually
            # Convert date column to string YYYYMMDD for comparison if needed, or just YYYY-MM-DD
            # stock_zh_index_daily returns date as object usually

            # Normalize columns
            # Result usually has: date, open, high, low, close, volume
            rename_map = {
                "date": "date",
                "open": "open",
                "high": "high",
                "low": "low",
                "close": "close",
                "volume": "volume"
            }
            # Ensure columns exist before rename (some versions use Chinese)
            # akshare typically uses English for this API, but let's be safe.
            # Actually stock_zh_index_daily returns: date, open, high, low, close, volume in recent versions.

        else:
            df = ak.stock_zh_a_hist(symbol=code, period=ak_period, start_date=start_date, end_date=end_date,
                                    adjust="qfq")

        if df.empty:
            raise HTTPException(status_code=404, detail=f"No data found for {symbol}")

        # AKShare 列名: 日期, 股票代码, 开盘, 收盘, 最高, 最低, 成交量, 成交额, 振幅, 涨跌幅, 涨跌额, 换手率
        # 重命名列以符合通用格式
        if not is_index:
            rename_map = {
                "日期": "date",
                "开盘": "open",
                "收盘": "close",
                "最高": "high",
                "最低": "low",
                "成交量": "volume",
                "成交额": "amount",
                "涨跌额": "change",
                "涨跌幅": "pct_chg",
                "换手率": "turnover"
            }

        df = df.rename(columns=rename_map)

        # Filter by date for Index data if needed (since API doesn't support start/end params directly sometimes)
        if is_index:
            # Convert date to datetime if not already
            if not pd.api.types.is_datetime64_any_dtype(df['date']):
                df['date'] = pd.to_datetime(df['date'])

            mask = (df['date'] >= pd.to_datetime(start_date)) & (df['date'] <= pd.to_datetime(end_date))
            df = df.loc[mask]

        # 转换日期格式 (如果是字符串无需转换，如果是日期对象转字符串)
        # akshare返回的日期通常是字符串 'YYYY-MM-DD'，但有时可能是 datetime
        if not pd.api.types.is_string_dtype(df['date']):
            df['date'] = df['date'].apply(lambda x: x.strftime('%Y-%m-%d') if hasattr(x, 'strftime') else str(x))

        # 选择需要的列并转换为字典列表
        # 确保包含: date, open, close, high, low, volume, turnover, pct_chg
        result_cols = ["date", "open", "close", "high", "low", "volume", "turnover", "pct_chg"]

        # 确保所有列都存在
        available_cols = [col for col in result_cols if col in df.columns]

        # 填充缺失值（可选）
        df = df[available_cols].fillna(0)

        result = df.to_dict(orient="records")

        return result

    except Exception as e:
        print(f"Error fetching stock data: {e}")
        raise HTTPException(status_code=500, detail=str(e))


if __name__ == "__main__":
    import uvicorn

    uvicorn.run(app, host="0.0.0.0", port=8002)
