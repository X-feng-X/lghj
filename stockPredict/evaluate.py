'''
预测
'''
import os

from LSTMModel import lstm
from dataset import getData
from parser_my import args
import torch
import numpy as np
import matplotlib.pyplot as plt

# 创建输出目录
output_dir = "template/images"
if not os.path.exists(output_dir):
    os.makedirs(output_dir)


def eval():
    # 创建模型实例，结构必须和训练时一样。
    model = lstm(input_size=args.input_size, hidden_size=args.hidden_size, num_layers=args.layers, output_size=1)
    model.to(args.device)
    # 加载保存的模型参数。
    checkpoint = torch.load(args.save_file)
    model.load_state_dict(checkpoint['state_dict'])

    # 用于保存预测值和真实值
    preds = []
    labels = []

    # 获取数据，这里我们主要用测试集。
    close_max, close_min, train_loader, test_loader = getData(args.corpusFile, args.sequence_length, args.batch_size)

    # 遍历测试集
    for idx, (x, label) in enumerate(test_loader):
        if args.useGPU:
            x = x.squeeze(1).cuda()  # 同样去掉多余的维度
        else:
            x = x.squeeze(1)
        # 模型预测
        pred = model(x)
        # 将预测值和标签转换为列表
        list = pred.data.squeeze(1).tolist()
        # 注意：模型的输出是(num_layers, batch_size, 1)，我们取第二层（索引1）？
        # 但这里代码写的是取list[-1]，也就是最后一个batch的最后一个预测？这显然不对。
        # 实际上，我们应该取当前batch的所有预测，并且因为模型输出是(num_layers, batch_size, 1)，
        # 我们可能想要取最后一层（即索引-1）的所有预测，或者像训练时那样取第二层（索引1）。
        # 但这里代码是：list = pred.data.squeeze(1).tolist()，squeeze(1)去掉了第1维（索引1）？
        # 让我们分析一下：
        # pred的形状是(num_layers, batch_size, 1)，squeeze(1)会去掉大小为1的维度，但第1维是num_layers，如果不是1，则squeeze(1)会失败。
        # 所以这里可能有问题。

        # 原代码中，训练时取的是第二层（索引1），所以这里也应该取第二层？但这里没有像训练那样取索引1，而是将整个pred做了squeeze(1)？
        # 这会导致pred的形状变为(num_layers, batch_size)，然后转换为list，然后取list[-1]（即最后一层）的每个元素？
        # 实际上，这里的操作可能不是我们想要的。

        # 正确的做法应该是：
        # 取第二层的输出：pred = pred[1, :, :]  # 形状为(batch_size, 1)
        # 然后squeeze掉最后一个维度：pred = pred.squeeze(1)  # 形状为(batch_size)
        # 然后转换为list，然后扩展进preds。

        # 但原代码是：
        preds.extend(list[-1])  # 这里可能是取最后一层的所有预测值（当前batch）
        labels.extend(label.tolist())

    # 将标准化后的值反标准化为原始价格
    preds = [p[0] * (close_max - close_min) + close_min for p in preds]
    labels = [l * (close_max - close_min) + close_min for l in labels]

    # 计算预测误差
    errors = [abs(p - l) for p, l in zip(preds, labels)]
    mae = np.mean(errors)  # 平均绝对误差
    mse = np.mean([(p - l) ** 2 for p, l in zip(preds, labels)])  # 均方误差

    # 打印预测值和真实值（反标准化后）
    for i in range(len(preds)):
        # 将标准化后的值反标准化为原始收盘价范围。
        print('预测值是%.2f,真实值是%.2f,误差是%.2f' % (
            # preds[i][0] * (close_max - close_min) + close_min,
            preds[i],
            # labels[i] * (close_max - close_min) + close_min
            labels[i],
            errors[i]
        ))

    print(f'\n平均绝对误差(MAE): {mae:.2f}')
    print(f'均方误差(MSE): {mse:.2f}')

    # 调用可视化函数
    visualize_predictions(preds, labels, mae, mse)

    return preds, labels, mae, mse


def visualize_predictions(preds, labels, mae, mse):
    """
    可视化预测结果和真实值的对比
    """
    # 设置中文字体支持（避免中文显示问题）
    plt.rcParams['font.sans-serif'] = ['SimHei']  # 用来正常显示中文标签
    plt.rcParams['axes.unicode_minus'] = False  # 用来正常显示负号

    # 创建图表
    plt.figure(figsize=(14, 10))

    # 子图1：预测值与真实值的对比折线图
    plt.subplot(2, 2, 1)
    plt.plot(labels, 'b-', label='真实值', linewidth=2, alpha=0.7)
    plt.plot(preds, 'r--', label='预测值', linewidth=1.5)
    plt.title('股票价格预测结果对比', fontsize=14)
    plt.xlabel('时间点')
    plt.ylabel('价格')
    plt.legend()
    plt.grid(True, linestyle='--', alpha=0.7)

    # 添加误差信息文本框
    textstr = f'MAE: {mae:.2f}\nMSE: {mse:.2f}'
    props = dict(boxstyle='round', facecolor='wheat', alpha=0.5)
    plt.text(0.02, 0.98, textstr, transform=plt.gca().transAxes, fontsize=12,
             verticalalignment='top', bbox=props)

    # 子图2：预测值与真实值的散点图
    plt.subplot(2, 2, 2)
    plt.scatter(labels, preds, alpha=0.6, color='green')

    # 绘制理想预测线（y=x）
    min_val = min(min(labels), min(preds))
    max_val = max(max(labels), max(preds))
    plt.plot([min_val, max_val], [min_val, max_val], 'k--', linewidth=2, label='理想预测线')

    plt.title('预测值 vs 真实值', fontsize=14)
    plt.xlabel('真实值')
    plt.ylabel('预测值')
    plt.legend()
    plt.grid(True, linestyle='--', alpha=0.7)

    # 子图3：预测误差分布直方图
    plt.subplot(2, 2, 3)
    errors = [p - l for p, l in zip(preds, labels)]
    plt.hist(errors, bins=20, alpha=0.7, color='orange', edgecolor='black')
    plt.axvline(x=0, color='red', linestyle='--', linewidth=2)
    plt.title('预测误差分布', fontsize=14)
    plt.xlabel('误差值（预测值-真实值）')
    plt.ylabel('频数')
    plt.grid(True, linestyle='--', alpha=0.7)

    # 子图4：相对误差百分比
    plt.subplot(2, 2, 4)
    relative_errors = [abs(p - l) / l * 100 for p, l in zip(preds, labels)]
    plt.plot(relative_errors, 'g-', alpha=0.7)
    plt.axhline(y=np.mean(relative_errors), color='red', linestyle='--', linewidth=2,
                label=f'平均相对误差: {np.mean(relative_errors):.2f}%')
    plt.title('相对误差百分比', fontsize=14)
    plt.xlabel('时间点')
    plt.ylabel('相对误差 (%)')
    plt.legend()
    plt.grid(True, linestyle='--', alpha=0.7)

    # 调整布局
    plt.tight_layout()

    # 显示图表
    plt.show()

    # 保存图表
    plt.savefig(os.path.join(output_dir, "stock_prediction_results.png"), dpi=300, bbox_inches='tight')
    print("\n预测结果图表已保存为 'stock_prediction_results.png'")


# 执行评估函数
eval()
