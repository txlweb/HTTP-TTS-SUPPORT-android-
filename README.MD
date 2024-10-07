# HTTP TTS API SUPPORT

这是一个本地HTTP TTS API,使用Summer TTS合成语音,内置了其作者公布的模型,使其能在android中运行.提供了一个HTTP接口.

## 例子:

> http://127.0.0.1:8088/?1.0?测试文本

这样会以1.0的语速播放文本 "测试文本"


## 生成速度

程序提供了一个基本测试,10为最快,0为最慢.

## 修改端口

程序提供了一个config.ini,可以在内部用其他软件打开(建议MT文件管理器编辑)

## 支持架构

arm64-v8a , x86 , x86_64

不支持32位(arm32-v7a)!!

## Summer TTS项目地址

[SummerTTS](https://github.com/huakunyang/SummerTTS)