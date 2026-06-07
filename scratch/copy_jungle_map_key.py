import shutil
import os
from PIL import Image

src = 'C:/Users/Admin/.gemini/antigravity/brain/e3f213dc-a81f-4220-8bc5-bf419fee6ae8/media__1780801351108.png'
dest_raw_dir = 'assets_raw/objects/jungle_map_key'
dest_maps_dir = 'assets/maps/objects'

os.makedirs(dest_raw_dir, exist_ok=True)
os.makedirs(dest_maps_dir, exist_ok=True)

shutil.copy(src, os.path.join(dest_raw_dir, 'jungle_map_key.png'))
shutil.copy(src, os.path.join(dest_maps_dir, 'jungle_map_key.png'))

print("Copied successfully.")
with Image.open(os.path.join(dest_raw_dir, 'jungle_map_key.png')) as img:
    print(f"Size: {img.size}, Format: {img.format}")
