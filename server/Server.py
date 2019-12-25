from flask import Flask,request
import cv2
import os
import threading
import dlib
import json
import time
from pynput.keyboard import Listener
import math
images =[]
track_result=[]
movement_sequance=[]
pre_box=None
basepath = os.path.dirname(os.path.abspath(__file__))
original_area = None
app = Flask(__name__)
move = {"mPitch": 0, "mRoll": 0, "mYaw": 0, "mThrottle": 0, "gPitch": 0, "gRoll": 0,
                                    "gYaw": 0}
name = 'testdata'
con_image = threading.Condition()
con_result = threading.Condition()


key ='DSST'

@app.route('/')
def hello_world():
    return 'Hello World!'



@app.route('/uploadImage',methods=['POST', 'GET'])
def uploadImage():
    f = request.files["files"]
    global basepath
    global images
    global movement_sequance
    global name
    filename = f.filename
    indirectpath = os.path.join(basepath, 'static')
    indirectpath = os.path.join(indirectpath, 'images')


    upload_path = os.path.join(indirectpath, name,filename)
    #print(upload_path)
    f.save(upload_path)
    images.append(upload_path)

    #if con_image.acquire():
        # 当获得条件变量后
    #con_image.notify()
    #con_image.release()

    return filename


@app.route('/getcontrol',methods=['POST', 'GET'])
def getcontrol():
    print(move)
    return json.dumps(move)


def load_images():
    global images
    global pre_box
    global move
    global original_area
    start_tracking = False
    first = True
    conter = 0
    tracker=None
    print("start handle image")
    while True:
       # if con_image.acquire():
            # 当获得条件变量后
            if len(images)==0:
                # 图像缓存中没有图片
                #print("it has been zero")
                #con_image.wait()
                time.sleep(0.08)
                # 该进程处于wait状态

            else:
                # 图像缓存中有图片
                # 取出图片并显示
                picpath = images[0]
                images.remove(picpath)

                img = cv2.imread(picpath)
                img = cv2.resize(img, (640, 360))
                #print(conter)
                print(picpath)
                #cv2.imshow('img', img)
                #os.remove(picpath)
                del img
                #if cv2.waitKey(1) & 0xff==ord('s'):

                # 通过notify方法通知上传进程
                #con_image.notify()
                conter+=1
        # 条件变量释放
        #con_image.release()


def press(key):
      print(key.char)
      if key.char == 'a':
          move['mYaw'] -= 1
          print("yaw down{}".format(move['mYaw']))
      if key.char == 'd':
          move['mYaw'] += 1
          print("yaw up{}".format(move['mYaw']))
      if key.char == 's':
          move['mPitch'] -= 0.1
          print("pitch down{}".format(move['mPitch']))
      if key.char == 'w':
          move['mPitch'] += 0.1
          print("pitch up{}".format(move['mPitch']))
      if key.char == 'q':
          move['mRoll'] -= 0.1
          print("roll down{}".format(move['mRoll']))
      if key.char == 'e':
          move['mRoll'] += 0.1
          print("roll up{}".format(move['mRoll']))
      if key.char == 'g':
          move['mThrottle'] -= 1
          print("high down{}".format(move['mThrottle']))
      if key.char == 't':
          move['mThrottle'] += 1
          print("high up{}".format(move['mThrottle']))
      if key.char == 'u':
          move['mYaw'] = 0
          move['mPitch'] = 0
          move['mRoll'] = 0
          move['mThrottle'] = 0

          print("reset")


def listen():
    with Listener(on_press = press) as listener:
        listener.join()



class myThread (threading.Thread):   #继承父类threading.Thread
    def __init__(self, threadID, name, func, param=None):
        threading.Thread.__init__(self)
        self.threadID = threadID
        self.name = name
        self.func = func
        self.param = param
    def run(self):
        print(self.func)
        if self.param==None:
            self.func()
        else:
            self.func(self.param)

if __name__ == '__main__':
    print(basepath)
    print("show info")
    indirectpath = os.path.join(basepath, 'static')
    indirectpath = os.path.join(indirectpath, 'images')
    #global name
    path = os.path.join(indirectpath, name)
    if not os.path.exists(path):
        os.mkdir(path)
    thread3 = myThread(3,"key",listen)
    thread2 = myThread(2,"show",load_images)
    thread1 = myThread(1,"flask",app.run,'0.0.0.0')

    thread3.start()
    thread1.start()
    thread2.start()

