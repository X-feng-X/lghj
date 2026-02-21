import argparse
import torch

# 创建一个ArgumentParser对象，这个对象将保存所有需要解析的命令行参数信息。
parser = argparse.ArgumentParser()

# 添加参数：--corpusFile，默认值是'data/000001SH_index.csv'，这是数据文件的路径。
parser.add_argument('--corpusFile', default='data/000001SH_index.csv')

# 以下是一些常改动的参数，我们为每个参数提供了默认值。

# --gpu: 指定使用哪一张GPU卡，默认是0号GPU。如果只有一张GPU，通常就是0。
parser.add_argument('--gpu', default=0, type=int)

# --epochs: 训练的总轮数。一轮代表模型已经看过整个训练集一遍。默认100轮。
parser.add_argument('--epochs', default=100, type=int)

# --layers: LSTM模型的层数。层数越多，模型越复杂，学习能力越强，但也更容易过拟合。默认2层。
parser.add_argument('--layers', default=2, type=int)

# --input_size: 输入特征的维度。在我们的数据中，经过预处理后，每天的数据有8个特征（比如开盘价、最高价、最低价、收盘价、成交量等）。所以默认是8。
parser.add_argument('--input_size', default=8, type=int)

# --hidden_size: LSTM隐藏层的维度，也可以理解为LSTM单元输出向量的长度。默认32。
parser.add_argument('--hidden_size', default=32, type=int)

# --lr: 学习率。控制模型参数更新的步长。太小会导致训练慢，太大会导致不稳定。默认0.0001。
parser.add_argument('--lr', default=0.0001, type=float)

# --sequence_length: 序列长度。我们用过去几天的数据来预测下一天？默认是5，即用5天的数据预测第6天。
parser.add_argument('--sequence_length', default=5, type=int)

# --batch_size: 批处理大小。一次训练中同时处理多少个序列。较大的batch_size可以加速训练，但需要更多内存。默认64。
parser.add_argument('--batch_size', default=64, type=int)

# --useGPU: 是否使用GPU。默认False，即使用CPU。如果你有GPU并且想用，可以设置为True。
parser.add_argument('--useGPU', default=False, type=bool)

# --batch_first: 是否将batch大小作为输入张量的第一维。默认True。PyTorch中LSTM的输入默认是(seq_len, batch, input_size)，如果batch_first=True，则输入为(batch, seq_len, input_size)。
parser.add_argument('--batch_first', default=True, type=bool)

# --dropout: Dropout率。在训练时，随机忽略一定比例的神经元，防止过拟合。默认0.1，即10%的神经元会被随机忽略。
parser.add_argument('--dropout', default=0.1, type=float)

# --save_file: 训练好的模型保存的路径。默认是'model/stock.pkl'。
parser.add_argument('--save_file', default='model/stock.pkl')

# 解析命令行参数，并将结果保存在args对象中。
args, _ = parser.parse_known_args()

# 设置设备（Device）：如果useGPU为True并且当前有可用的GPU，则使用指定的GPU；否则使用CPU。
device = torch.device(f"cuda:{args.gpu}" if torch.cuda.is_available() and args.useGPU else "cpu")
# 将设备信息也存入args对象中，这样其他文件就可以通过args.device来使用这个设备。
args.device = device
