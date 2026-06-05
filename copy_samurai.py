import os
from PIL import Image

src_dir = "Ninja Adventure - Asset Pack/giant_blue_samurai"
dest_dir = "assets_raw/objects/GiantBlueSamurai_Idle_00"
map_preview_dir = "assets/maps/objects"

os.makedirs(dest_dir, exist_ok=True)
os.makedirs(map_preview_dir, exist_ok=True)

def process_file(src_name, dest_name, flip=False):
    src_path = os.path.join(src_dir, src_name)
    dest_path = os.path.join(dest_dir, dest_name)
    if os.path.exists(src_path):
        img = Image.open(src_path)
        if flip:
            img = img.transpose(Image.FLIP_LEFT_RIGHT)
        img.save(dest_path)

# 1. Idle (6 frames: 00 to 05)
for i in range(6):
    # Assume default Idle faces left (based on attackleft)
    process_file(f"GiantBlueSamurai_Idle_0{i}.png", f"idle_left_0{i}.png", flip=False)
    process_file(f"GiantBlueSamurai_Idle_0{i}.png", f"idle_right_0{i}.png", flip=True)
    process_file(f"GiantBlueSamurai_Idle_0{i}.png", f"idle_down_0{i}.png", flip=False)
    process_file(f"GiantBlueSamurai_Idle_0{i}.png", f"idle_up_0{i}.png", flip=True)

# 2. Walk (6 frames: 00 to 05)
for i in range(6):
    process_file(f"GiantBlueSamurai_Walk_0{i}.png", f"walk_left_0{i}.png", flip=False)
    process_file(f"GiantBlueSamurai_Walk_0{i}.png", f"walk_right_0{i}.png", flip=True)
    process_file(f"GiantBlueSamurai_Walk_0{i}.png", f"walk_down_0{i}.png", flip=False)
    process_file(f"GiantBlueSamurai_Walk_0{i}.png", f"walk_up_0{i}.png", flip=True)

# 3. Attack Left (7 frames: 00 to 06)
for i in range(7):
    process_file(f"GiantBlueSamurai_attackleft_0{i}.png", f"attack_left_0{i}.png", flip=False)
    process_file(f"GiantBlueSamurai_attackleft_0{i}.png", f"attack_down_0{i}.png", flip=False)

# 4. Attack Right (7 frames: 00 to 06)
for i in range(7):
    process_file(f"GiantBlueSamurai_attackright_0{i}.png", f"attack_right_0{i}.png", flip=False)
    process_file(f"GiantBlueSamurai_attackright_0{i}.png", f"attack_up_0{i}.png", flip=False)

# 5. Damaged (Hit) (4 frames: 00 to 03)
for i in range(4):
    process_file(f"GiantBlueSamurai_Hit_0{i}.png", f"damaged_left_0{i}.png", flip=False)
    process_file(f"GiantBlueSamurai_Hit_0{i}.png", f"damaged_right_0{i}.png", flip=True)
    process_file(f"GiantBlueSamurai_Hit_0{i}.png", f"damaged_down_0{i}.png", flip=False)
    process_file(f"GiantBlueSamurai_Hit_0{i}.png", f"damaged_up_0{i}.png", flip=True)

# 6. Dead (use last frame of Hit)
process_file("GiantBlueSamurai_Hit_03.png", "dead_left_00.png", flip=False)
process_file("GiantBlueSamurai_Hit_03.png", "dead_right_00.png", flip=True)
process_file("GiantBlueSamurai_Hit_03.png", "dead_down_00.png", flip=False)
process_file("GiantBlueSamurai_Hit_03.png", "dead_up_00.png", flip=True)

# 7. Copy map preview (use Idle_00)
preview_src = os.path.join(src_dir, "GiantBlueSamurai_Idle_00.png")
preview_dest = os.path.join(map_preview_dir, "GiantBlueSamurai_Idle_00.png")
if os.path.exists(preview_src):
    img = Image.open(preview_src)
    img.save(preview_dest)

print("GiantBlueSamurai assets processed successfully!")
