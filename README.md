# MCdisplay
MCdisplay is a modern and fast bukkit plugin for rendering any image or video from your drive in a 1:1 scale to a display in Minecraft!

## Features

  * display any image or video on a variable resolution screen
    * you can change the resolution of the screen, your image or video will automatically get scaled to the right size to fit the screen!
  * 1 pixel = 1 block
  * supports 35 colors/blocks (for now, I will add more in future updates!)
  * play videos at 20fps!
  * fast rendering in the background, so as few laggs as possible
  * tab completion that shows you your saved images and videos
  * very easy to use
  
## Installation
  * First of all, you need a Craftbukkit or Spigot server that ran at least once and created all the necessary files and folders
  * If you have that, head over to the releases page and download the latest release, unzip the folder and copy everything into the server's plugin folder
  * Inside the plugins folder, there should now be a file called "MCdisplay.jar" as well as a folder called "MCdisplay". Inside that folder should be a file called "resize.exe" (don't be suprised by the size of this file, this is my python backend used to resize images and videos, compiled into an .exe by pyinstaller and the libraries used are quite big, if you don't want to use the big .exe, replace the MCdisplay.jar with the latest MCdisplay.jar from "latest build" and copy the resize.py into the directory where the .exe is) and the two folders "image" and "video". Now just put all images you want to be able to render in Minecraft into the "image" folder, same thing with videos.
  * Nice, now you are ready to run the server and render your first image!
  * Start your server and join the world. Once you're on the server, you can simply use `/display` to access all MCdisplay commands, the tab completion will show you what commands are available and they are explained in the "Usage" section below.
  
## Usage
  * Use the display in a superflat world with structures disabled
  * The screen will be located at 0 0 in your world, so first teleport there.
  * The default resolution is 256x144, which the resolution will be reset to every time you restart the server.
  * Images or Videos with high contrast work best as the display doesn't support that many colors just yet. There are some exaples in the folders to test out!
  ### Images
  To display an image, you first have to put it in the "image" folder as described above. 
  After that, you can simply render it by typing `/display image <YourImage.jpg>`. The tab completion 
  will show you what files you are able to render.
  Not all image formats are supported, for example not supported are .webp images.
  ### Videos
  To play a video, you first have to put it in the "video" folder as described above. 
    After that, just type `/display video <YourVideo.mp4>` to start rendering a video. 
    Videos are handled a little diffrently than images. Images get rendered and instantly displayed while videos will be first rendered completely frame-by-frame. This is to enable video playback of up to 20fps, Minecrafts limit for placing blocks.
    There will be a message in the chat when your video is ready and will start playing, it takes about 30 seconds to render a 5 minute video in 20fps (at 128x72), so you don't have to wait too long!
    Videos have a maximum resolution of 256 in heigth as they get display vertically instead of horizontally as images, this is to reduce the amount of chunks that have to be updated each frame.
    Videos also just play in **full 20fps at 128x72 or lower**, as there are too many chunk updates for Minecraft to perform in time in higher resolutions which results in laggy frames or frames that just donÄt get rendered completely.
    The plugin will automatically resize the display for videos if they are too big and also automatically detects what framerate to use.
  ### Pause a video
  You can pause a video playing by typing `/display pause` and resume it ba typing `/display pause` again.
  ### Replay a video
  You can replay the last video you played by either typing in the video play command with the same filename again or by typing `/display replay`. The last video gets stored in memory, so it can be played again instantly!
  ### Stop a video
  You can also stop the video playing completely by using `/display stop`. This will stop the video playback and it can't be continued from the frame it is at.
  ### Change resolution
  Changing resolution is as simple as typing `/display resolution <width>x<height>`, the plugin will suggest you some 16:9 resolutions you can select so you don't have to look them up. You can change to any resolution and aspect resolution though, I wouldn't recommend going higher than 1280x720.
    The screen will despawn and then spawn again which can lagg the server for a few seconds. Your videos and images will automatically get scaled to the highest resolution possible to fit your current resolution and aspect ratio.
  ### Turn display on/off
  You can spawn/despan the display by typing `/display <on/off>`, that will either spawn or remove the display in the current resolution.

## Development
  For development, you need a Java 8 JDK as well as python. Clone this repository to your machine and then install the python requirements via `pip install -r requirements.txt` from the root of this project.
