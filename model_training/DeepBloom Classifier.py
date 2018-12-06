#!/usr/bin/env python
# coding: utf-8

# # Learning a Binary Classifier

# ## Importing Data

# In[1]:


import pandas as pd


# In[2]:


df = pd.read_csv("data.csv")


# In[3]:


df


# ## Deep Learning Deep Shit

# In[4]:


from collections import Counter

import keras
import keras.backend as k
import numpy as np
import tensorflow as tf
from keras.layers import (GRU, LSTM, BatchNormalization, Bidirectional, Dense,
                          Dropout)
from keras.models import Sequential
from keras.preprocessing.sequence import pad_sequences
from keras.preprocessing.text import Tokenizer
from keras.utils import multi_gpu_model, to_categorical
from sklearn.model_selection import train_test_split


# In[5]:


# TensorFlow wizardry
config = tf.ConfigProto()
 
# Don't pre-allocate memory; allocate as-needed
config.gpu_options.allow_growth = True
 
# Create a session with the above options specified.
k.tensorflow_backend.set_session(tf.Session(config=config))


# In[6]:


from tensorflow.python.client import device_lib
print(device_lib.list_local_devices())


# In[7]:


BATCH_SIZE = 1024  # batch size for the network
EPOCH_NUMBER = 1  # number of epochs to train
THRESHOLD = 5  # symbols appearing fewer times will be replaced by a placeholder


# ## Preparing Data

# In[8]:


unique_symbols = Counter()

for _, message in df['url'].iteritems():
    unique_symbols.update(message)
    
print("Unique symbols:", len(unique_symbols))


# Find symbols that appear fewer times than the threshold:

uncommon_symbols = list()

for symbol, count in unique_symbols.items():
    if count < THRESHOLD:
        uncommon_symbols.append(symbol)

print("Uncommon symbols:", len(uncommon_symbols))


# In[9]:


# Replace them with a placeholder:
DUMMY = uncommon_symbols[0]
tr_table = str.maketrans("".join(uncommon_symbols), DUMMY * len(uncommon_symbols))

df['url'] = df['url'].apply(lambda x: x.translate(tr_table))

# We will need the number of unique symbols further down when we will decide on the dimensionality of inputs.
num_unique_symbols = len(unique_symbols) - len(uncommon_symbols) + 1 


# In[10]:


tokenizer = Tokenizer(char_level=True)
tokenizer.fit_on_texts(df['url'])


# In[11]:


mat = tokenizer.texts_to_sequences(df['url'])
mat = pad_sequences(mat)


# In[12]:


y = df['label']
y = (y=="bad")


# In[13]:


x_train, x_test, y_train, y_test = train_test_split(mat, y, stratify=y.values, test_size=0.10)


# In[14]:


x_train.shape, x_test.shape


# ## Defining Model

# In[ ]:


class DataGenerator(keras.utils.Sequence):
    'Generates data for Keras'
    def __init__(self, data, labels, batch_size=32, dim=(32,32,32), n_channels=1, n_classes=10, shuffle=True):
        'Initialization'
        self.dim = dim
        self.batch_size = batch_size
        self.labels = labels
        self.data = data
        self.n_channels = n_channels
        self.n_classes = n_classes
        self.shuffle = shuffle
        self.indexes = np.arange(data.shape[0])
        self.on_epoch_end()

    def __len__(self):
        'Denotes the number of batches per epoch'
        return int(np.floor(len(self.indexes) / self.batch_size))

    def __getitem__(self, index):
        'Generate one batch of data'
        # Generate indexes of the batch
        batch = self.data[index:index+self.batch_size]
        batch = to_categorical(batch, num_classes=num_unique_symbols)
        y_batch = self.labels.iloc[index:index+self.batch_size]
        return batch, y_batch

    def on_epoch_end(self):
        'Updates indexes after each epoch'
        if self.shuffle == True:
            np.random.shuffle(self.indexes)


# In[15]:


sequence_length = x_train.shape[1]


# In[19]:


model = Sequential()
model.add(LSTM(100, input_shape=(sequence_length, num_unique_symbols), activation="tanh", return_sequences=True))
model.add(BatchNormalization())
model.add(Dropout(0.2))
model.add(LSTM(50, input_shape=(sequence_length, num_unique_symbols), activation="tanh"))
model.add(Dropout(0.2))
model.add(Dense(25, activation="tanh"))
model.add(Dropout(0.2))
model.add(Dense(1, activation="sigmoid"))


# In[20]:


model.compile(optimizer="adam", loss="binary_crossentropy", metrics=['accuracy'])


# In[ ]:


EPOCH_NUMBER = 20


# In[ ]:


parallel_model = multi_gpu_model(model, gpus=2)
parallel_model.compile(optimizer="adam", loss="binary_crossentropy", metrics=['accuracy'])


# In[ ]:


dg = DataGenerator(x_train, y_train, batch_size=128)


# In[ ]:


model.fit_generator(dg, epochs=10, use_multiprocessing=True, workers=12)


# In[ ]:


parallel_model.fit_generator(dg, epochs=10, use_multiprocessing=True, workers=6)


# In[ ]:


x_train.shape


# In[37]:


model.save("best_model.h5")


# In[38]:


import gc
gc.collect()


# In[ ]:


BATCH_SIZE = 10240
for epoch in range(EPOCH_NUMBER):
    print("Epoch", epoch)
    for i in range(340992, len(x_train), BATCH_SIZE):
        batch = x_train[i:i+BATCH_SIZE]
        batch = to_categorical(batch, num_classes=num_unique_symbols)
        y_batch = y_train.iloc[i:i+BATCH_SIZE]
        model.fit(batch, y_batch, batch_size=256, validation_split=0.1)


# In[34]:


x_test_cate = to_categorical(x_test[5000:10000], num_classes=num_unique_symbols)


# In[35]:


model.evaluate(x_test_cate, y_test[5000:10000], batch_size=128)


# In[ ]:


model.fit(x_train, y_train, validation_split=0.1, batch_size=32)

