#!/usr/bin/python
# -*- coding: utf-8 -*-
import linecache
import random
import json

def get_proxy_ip():
    a = random.randrange(1,200);
    f = open('ips.txt','r');
    theline = linecache.getline(r'ips.txt', a).replace("\n","");
    result = '{"http":"http://' + theline + '"}';
    return eval(result);

if __name__ == '__main__':
    print(get_proxy_ip())     # 打印出获取到的随机代理IP
