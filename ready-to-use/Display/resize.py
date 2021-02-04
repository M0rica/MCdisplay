import os
from PIL import Image
import cv2
import argparse
from cv2 import data
import numpy as np
import json


def resize_image(img: Image, resolution: tuple):
    img = img.convert('RGB')
    img.thumbnail(resolution, Image.ANTIALIAS)
    new_img = Image.new('RGB', resolution, (0, 0, 0))
    new_img.paste(
        img, (int((resolution[0] - img.size[0]) / 2), int((resolution[1] - img.size[1]) / 2))
    )
    return new_img

def resize_image_from_disk(path: str, resolution: tuple):
    img = resize_image(Image.open(path), resolution)
    img.save(f'plugins/Display/resized/{path.split("/")[-1]}')
    print("Saved")

def resize_video(path: str, resolution: tuple):
    vidcap = cv2.VideoCapture(path)
    success, img = vidcap.read()
    data_list = []
    frames = 0

    #output_video = cv2.VideoWriter(f'plugins/Display/resized/{path.split("/")[-1]}', 0, 20, resolution)
    while success:
        img = resize_image(Image.fromarray(img), resolution)
        img = np.asarray(img)
        #output_video.write(img)
        img_list = img.tolist()
        frame = []
        #print(len(img_list), len(img_list[0]))
        for i in range(0, len(img_list)):
            line = []
            for j in range(0, len(img_list[0])):
                current = img_list[i][j]
                line.append(current[0]*current[1]*current[2])
            frame.append(line)
        frames += 1

        data_list.append(frame)


        success, img = vidcap.read()
    
    #cv2.destroyAllWindows()
    #output_video.release()
    #print(data_list[0])

    save_data = {
        'frames': frames,
        'data': data_list
    }
    with open('plugins/Display/resized/video.json', 'w') as f:
        json.dump(save_data, f, indent=4)

parser = argparse.ArgumentParser(description='Argparser for resize')

parser.add_argument('file', action='store')
parser.add_argument('resolution', action='store')

args = parser.parse_args()
file = args.file
resolution = [int(a) for a in args.resolution.split('x')]

print(file, resolution)

if not os.path.isdir('plugins/Display/resized'):
    os.mkdir('plugins/Display/resized')

if len(os.listdir('plugins/Display/resized')) > 1:
    for f in os.listdir("plugins/Display/resized"):
        os.remove(f'plugins/Display/resized/{f}')

if file.split('/')[2] == 'image':
    resize_image_from_disk(file, tuple(resolution))

elif file.split('/')[2] == 'video':
    resize_video(file, tuple(resolution))