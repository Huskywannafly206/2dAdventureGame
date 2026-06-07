from PIL import Image
import os

path = 'assets_raw/objects/jungle_map_key/jungle_map_key.png'
dest_raw_path = 'assets_raw/objects/jungle_map_key/jungle_map_key.png'
dest_maps_path = 'assets/maps/objects/jungle_map_key.png'

with Image.open(path) as img:
    # Create new 16x16 transparent image
    new_img = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
    # Center paste
    new_img.paste(img, (1, 1))
    new_img.save(dest_raw_path)
    new_img.save(dest_maps_path)

print("Padded to 16x16 successfully.")
