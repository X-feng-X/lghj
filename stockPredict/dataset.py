from pandas import read_csv  # 导入pandas库的read_csv函数，用于读取CSV文件。
import numpy as np  # 导入numpy库，用于数值计算。
from torch.utils.data import DataLoader, Dataset  # 从PyTorch导入DataLoader和Dataset，用于构建数据管道。
import torch  # 导入PyTorch。
from torchvision import transforms  # 导入transforms，用于数据转换（这里用了ToTensor）。
from parser_my import args  # 从parser_my.py中导入args对象，这样我们就可以使用前面设置的参数。


# 定义getData函数，它接收三个参数：数据文件路径corpusFile、序列长度sequence_length、批大小batchSize。
def getData(corpusFile, sequence_length, batchSize):
    # 读取CSV文件，将数据加载到stock_data（DataFrame对象）。
    stock_data = read_csv(corpusFile)
    # 删除不需要的列。inplace=True表示直接修改原DataFrame。
    stock_data.drop('ts_code', axis=1, inplace=True)  # 删除股票代码列
    stock_data.drop('id', axis=1, inplace=True)  # 删除id列
    stock_data.drop('pre_close', axis=1, inplace=True)  # 删除前收盘价列
    stock_data.drop('trade_date', axis=1, inplace=True)  # 删除交易日期列

    # 获取收盘价的最大值和最小值，用于后续的反标准化（将预测值变回原始范围）。
    close_max = stock_data['close'].max()  # 收盘价的最大值
    close_min = stock_data['close'].min()  # 收盘价的最小值
    # 对每一列进行min-max标准化，将每个特征缩放到[0,1]区间。
    df = stock_data.apply(lambda x: (x - min(x)) / (max(x) - min(x)))

    # 构造输入序列X和标签Y。
    sequence = sequence_length  # 序列长度
    X = []  # 用于存储输入序列
    Y = []  # 用于存储对应的标签（下一天的收盘价）
    # 遍历整个DataFrame，从第0行到倒数第sequence+1行（因为我们要取sequence_length天的数据）
    for i in range(df.shape[0] - sequence):
        # X: 从第i行到第i+sequence-1行（共sequence天）的所有列（特征）作为输入序列。
        X.append(np.array(df.iloc[i:(i + sequence), ].values, dtype=np.float32))
        # Y: 第i+sequence行的第0列（即收盘价）作为标签。
        Y.append(np.array(df.iloc[(i + sequence), 0], dtype=np.float32))

    # 计算总样本数
    total_len = len(Y)
    # 将99%的数据作为训练集，1%作为测试集。这里没有验证集。
    trainx, trainy = X[:int(0.99 * total_len)], Y[:int(0.99 * total_len)]
    testx, testy = X[int(0.99 * total_len):], Y[int(0.99 * total_len):]

    # 创建训练集的数据加载器（DataLoader）。
    # 使用Mydataset类封装训练数据，并应用transforms.ToTensor()将数据转为Tensor。
    train_loader = DataLoader(dataset=Mydataset(trainx, trainy, transform=transforms.ToTensor()), batch_size=batchSize,
                              shuffle=True)
    # 创建测试集的数据加载器，测试集不需要转换（因为Mydataset类中已经处理了）。
    test_loader = DataLoader(dataset=Mydataset(testx, testy), batch_size=batchSize, shuffle=True)
    # 返回收盘价的最大最小值（用于后续反标准化）以及训练和测试数据加载器。
    return close_max, close_min, train_loader, test_loader


# 自定义数据集类Mydataset，继承自PyTorch的Dataset类。
class Mydataset(Dataset):
    # 初始化方法，接收数据xx（特征），yy（标签），以及可选的转换transform。
    def __init__(self, xx, yy, transform=None):
        self.x = xx  # 存储特征数据
        self.y = yy  # 存储标签数据
        self.tranform = transform  # 存储转换方法

    # 支持索引操作，使得可以通过索引获取数据样本。
    def __getitem__(self, index):
        x1 = self.x[index]  # 获取第index个样本的特征
        y1 = self.y[index]  # 获取第index个样本的标签
        # 如果定义了转换，则对特征应用转换（例如转为Tensor）
        if self.tranform != None:
            return self.tranform(x1), y1
        # 否则直接返回特征和标签
        return x1, y1

    # 返回数据集的大小
    def __len__(self):
        return len(self.x)
