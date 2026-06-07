import shutil
import os
from PIL import Image

src_dir = 'Ninja Adventure - Asset Pack/Items/Weapons/Stick'
dest_raw_dir = 'assets_raw/objects/weapon_rusty_sword'
dest_maps_dir = 'assets/maps/objects'

os.makedirs(dest_raw_dir, exist_ok=True)
os.makedirs(dest_maps_dir, exist_ok=True)

# Copy Sprite.png to weapon_rusty_sword.png in raw and maps
shutil.copy(os.path.join(src_dir, 'Sprite.png'), os.path.join(dest_raw_dir, 'weapon_rusty_sword.png'))
shutil.copy(os.path.join(src_dir, 'Sprite.png'), os.path.join(dest_maps_dir, 'weapon_rusty_sword.png'))

# Copy SpriteInHand.png to sprite_in_hand.png in raw
shutil.copy(os.path.join(src_dir, 'SpriteInHand.png'), os.path.join(dest_raw_dir, 'sprite_in_hand.png'))

print("Copied files successfully.")

for path in [
    os.path.join(dest_raw_dir, 'weapon_rusty_sword.png'),
    os.path.join(dest_raw_dir, 'sprite_in_hand.png')
]:
    with Image.open(path) as img:
        print(f"{path}: size={img.size}, format={img.format}")
