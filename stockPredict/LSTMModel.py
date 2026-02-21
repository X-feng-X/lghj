import torch.nn as nn  # 导入PyTorch的神经网络模块。


# 定义LSTM模型类，继承自nn.Module。
class lstm(nn.Module):
    # 初始化方法，定义模型的各个层和参数。
    def __init__(self, input_size=8, hidden_size=32, num_layers=1, output_size=1, dropout=0, batch_first=True):
        super(lstm, self).__init__()  # 调用父类的初始化方法。
        # 保存参数到实例中。
        self.hidden_size = hidden_size
        self.input_size = input_size
        self.num_layers = num_layers
        self.output_size = output_size
        self.dropout = dropout
        self.batch_first = batch_first

        # 定义LSTM层。
        # 参数说明：
        # input_size: 输入的特征维度
        # hidden_size: 隐藏层的维度
        # num_layers: 层数
        # batch_first: 如果为True，则输入和输出的张量形状为(batch, seq, feature)
        # dropout: 如果非零，则在除最后一层外的每个LSTM层输出后应用dropout
        self.rnn = nn.LSTM(
            input_size=self.input_size,
            hidden_size=self.hidden_size,
            num_layers=self.num_layers,
            batch_first=self.batch_first,
            dropout=self.dropout
        )

        # 定义线性层（全连接层），将LSTM的输出（隐藏状态）映射到输出维度（1）。
        self.linear = nn.Linear(self.hidden_size, self.output_size)

    # 定义前向传播过程，即数据如何通过模型。
    def forward(self, x):
        # x的形状：如果batch_first=True，则为(batch_size, sequence_length, input_size)
        # 将x输入LSTM层，得到：
        # out: 所有时间步的隐藏状态，形状为(batch_size, sequence_length, hidden_size)
        # (hidden, cell): 最后一个时间步的隐藏状态和细胞状态，hidden和cell的形状都是(num_layers, batch_size, hidden_size)
        out, (hidden, cell) = self.rnn(x)

        # 这里，我们使用最后一个时间步的隐藏状态（hidden）来预测。
        # 注意：hidden包含每一层的最后一个时间步的隐藏状态，所以如果有多层，hidden的每一行代表一层。
        # 我们通常取最后一层的隐藏状态（即hidden[-1]）来进行预测，但这里直接使用了hidden（所有层）？
        # 实际上，下面的代码可能有问题，因为hidden的形状是(num_layers, batch_size, hidden_size)，
        # 而线性层的输入期望是(*, hidden_size)，所以这里应该取最后一层，即hidden[-1]。
        # 但原代码是：out = self.linear(hidden)
        # 这样线性层会作用在每一层上，输出形状为(num_layers, batch_size, output_size)。
        # 而原代码返回的是这个输出，这可能导致在训练和评估时取的是哪一层的输出不明确。

        # 一种常见的做法是使用最后一层的隐藏状态：
        # out = self.linear(hidden[-1])  # 取最后一层，形状为(batch_size, output_size)

        # 但原代码没有这样做，而是直接使用hidden。我们注意原代码在训练时（train.py）取了pred[1, :, :]，
        # 也就是说，它假设hidden有两层（num_layers=2），然后取第二层（索引1）的输出。这要求num_layers至少为2。

        # 因此，这里我们保持原代码不变，但注意在训练时取的是第二层。
        out = self.linear(hidden)

        return out
