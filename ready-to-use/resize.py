import os
from PIL import Image
import cv2
import argparse
import numpy as np


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

    output_video = cv2.VideoWriter(f'plugins/Display/resized/{path.split("/")[-1]}', 0, 20, resolution)
    while success:
        img = resize_image(Image.fromarray(img), resolution)
        img = np.asarray(img)

        output_video.write(img)

        success, img = vidcap.read()
    
    cv2.destroyAllWindows()
    output_video.release()

parser = argparse.ArgumentParser(description='Argparser for resize')

parser.add_argument('file', action='store')
parser.add_argument('resolution', action='store')

args = parser.parse_args()
file = args.file
resolution = [int(a) for a in args.resolution.split('x')]

print(file, resolution)

if not os.path.isdir('plugins/Display/resized'):
    os.mkdir('plugins/Display/resized')

if len(os.listdir('plugins/Display/resized')) == 1:
    os.remove(f'plugins/Display/resized/{os.listdir("plugins/Display/resized")[0]}')

if file.split('/')[2] == 'image':
    resize_image_from_disk(file, tuple(resolution))

elif file.split('/')[2] == 'video':
    resize_video(file, tuple(resolution))