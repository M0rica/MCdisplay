import os
from PIL import Image
from cv2 import VideoCapture, CAP_PROP_FPS
import argparse
import json

dir_path = 'plugins/MCdisplay'

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
    img.save(f'{dir_path}/resized/{path.split("/")[-1]}')
    print("Saved")

def resize_video(path: str, resolution: tuple, target_fps):
    vidcap = VideoCapture(path)
    fps = vidcap.get(CAP_PROP_FPS)
    #target_fps = 10
    print(fps)
    success, img = vidcap.read()
    frames = 1
    saved_frames = 0

    while success:
        img = resize_image(Image.fromarray(img), resolution)
        if round(frames%(fps/target_fps)) == 0:
            img.save(f'{dir_path}/resized/{path.split("/")[-1].split(".")[0]}_{saved_frames}.jpg')
            saved_frames += 1
        frames += 1
        success, img = vidcap.read()
    
    save_data = {
        'frames': saved_frames
    }
    with open(f'{dir_path}/resized/video.json', 'w') as f:
        json.dump(save_data, f, indent=4)


parser = argparse.ArgumentParser(description='Argparser for resize')

parser.add_argument('file', action='store')
parser.add_argument('resolution', action='store')
parser.add_argument('--fps', action='store', default='20')

args = parser.parse_args()
file = args.file
resolution = [int(a) for a in args.resolution.split('x')]
fps = int(args.fps)

print(file, resolution)

for d in ['resized', 'image', 'video']:
    if not os.path.isdir(f'{dir_path}/{d}'):
        os.mkdir(f'{dir_path}/{d}')

if len(os.listdir(f'{dir_path}/resized')) >= 1:
    for f in os.listdir(f'{dir_path}/resized'):
        os.remove(f'{dir_path}/resized/{f}')

print(os.listdir(f'{dir_path}/resized'))
if file.split('/')[2] == 'image':
    resize_image_from_disk(file, tuple(resolution))

elif file.split('/')[2] == 'video':
    resize_video(file, tuple(resolution), fps)