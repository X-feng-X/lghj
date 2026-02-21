from torch.autograd import Variable  # Variable已不推荐使用，现在一般直接使用Tensor。
import torch.nn as nn
import torch
from LSTMModel import lstm  # 导入我们定义的LSTM模型
from parser_my import args  # 导入参数
from dataset import getData  # 导入getData函数


def train():
    # 创建模型实例，传入参数。
    model = lstm(input_size=args.input_size, hidden_size=args.hidden_size, num_layers=args.layers, output_size=1,
                 dropout=args.dropout, batch_first=args.batch_first)
    # 将模型移动到设备（GPU或CPU）上。
    model.to(args.device)

    # 定义损失函数：均方误差损失（MSE）。
    criterion = nn.MSELoss()
    # 定义优化器：Adam，传入模型参数和学习率。
    optimizer = torch.optim.Adam(model.parameters(), lr=args.lr)

    # 获取数据：收盘价的最大最小值（用于反标准化）、训练数据加载器、测试数据加载器。
    close_max, close_min, train_loader, test_loader = getData(args.corpusFile, args.sequence_length, args.batch_size)

    # 开始训练循环，共args.epochs轮。
    for i in range(args.epochs):
        total_loss = 0  # 记录这一轮的总损失
        # 遍历训练数据加载器，每次获取一个batch的数据。
        for idx, (data, label) in enumerate(train_loader):
            # 根据是否使用GPU处理数据
            if args.useGPU:
                # 数据原本的形状是(batch_size, 1, sequence_length, input_size)？因为Mydataset中用了ToTensor()，可能会增加一个维度。
                # 所以用squeeze(1)去掉第1维（索引从0开始）的1。
                data1 = data.squeeze(1).cuda()
                # 将数据包装为Variable（老版本做法，现在已不需要），然后传入模型，得到预测值。
                pred = model(Variable(data1).cuda())
                # 从预测结果中取第二层（索引1）的输出。注意：模型的输出形状为(num_layers, batch_size, 1)
                pred = pred[1, :, :]
                # 将标签增加一个维度，从(batch_size)变为(batch_size, 1)，以匹配预测值的形状。
                label = label.unsqueeze(1).cuda()
            else:
                # CPU上的相同操作
                data1 = data.squeeze(1)
                pred = model(Variable(data1))
                pred = pred[1, :, :]
                label = label.unsqueeze(1)

            # 计算损失
            loss = criterion(pred, label)

            # 优化器梯度清零
            optimizer.zero_grad()
            # 反向传播，计算梯度
            loss.backward()
            # 更新模型参数
            optimizer.step()

            # 累加损失
            total_loss += loss.item()

        # 打印这一轮的总损失
        print("总损失", total_loss)

        # 每10轮保存一次模型
        if i % 10 == 0:
            # 保存模型的状态字典（推荐方式），这样我们可以只保存模型参数，不保存整个模型结构，便于加载。
            torch.save({'state_dict': model.state_dict()}, args.save_file)
            print('第%d epoch，保存模型' % i)

    # 训练结束后保存最终模型
    torch.save({'state_dict': model.state_dict()}, args.save_file)


# 执行训练函数
train()
