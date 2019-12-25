# AndroidUAVLink
 for control and get image

# 手机端主要活动
`connectactivity`
做连接测试包括
1. app注册key
2. 个人账户
3. 对应URL的网络连接（静态局域网ip,测试是否可以ping通）

除以上任务，如果没连接成功按钮没有可选，可以先用dji go官方程序进行连接测试，在官方程序大疆spark全套设备连接成功后再关掉官方程序打开此APP。

`mainactivity`
连接成功后的主要活动，基于包括几个解码选项以及回传的图像界面，以及上传图像及获取控制信息的网络要求及接口调用都在该层。 scream shot 为具体的开启传输。
基于[大疆视频流解码教程](https://github.com/DJI-Mobile-SDK-Tutorials/Android-VideoStreamDecodingSample)

`controller`
人手工调控制参数，跟mainactivity·里面的参数一致，方便进行控制测试

# 服务端主要功能

主要接收手机端的信号进行处理，包括上传图片和具体移动信号获得的请求（/uploadimage /getcontrol）

另外有两个函数

1. listen
监听部分键盘输入，以此修改控制信号

2. load_image
加载上传的图片，进行处理

# 暂时连接方法

经测试无人机的wifi网域实际暂不可笔记本连接，所以暂使用官方遥控器作为，无人机，手机，笔记本三者的转接网域。

##### 开启流程

1. 开启遥控器
2. 开启笔记本服务器，笔记本连接遥控器wifi, 查询自己分配到的ip
3. 在源程序中修改手机app的连接ip,编译安装程序，打开程序，此时连公网，解决账号登录问题
4. 然后手机转连遥控器wifi，观察或者用软件观察是否成功连上笔记本服务
5. 打开无人机，打开dji go 官方程序，选择晓，选择全套连接，按流程长按无人机按键对频，并在界面上显示连接成功
6. 关掉dji go，转到自己写的程序，此时发现已然连接成功，按钮变蓝


