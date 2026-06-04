import os
from PIL import Image

def slice_npc(char_name, out_folder_name):
    base_dir = f"Ninja Adventure - Asset Pack/Actor/Character/{char_name}/SeparateAnim"
    out_dir = f"assets_raw/objects/{out_folder_name}"
    os.makedirs(out_dir, exist_ok=True)
    
    shadow_path = "Ninja Adventure - Asset Pack/Actor/Character/Shadow.png"
    shadow = Image.open(shadow_path).convert("RGBA")
    
    idle_path = os.path.join(base_dir, "Idle.png")
    walk_path = os.path.join(base_dir, "Walk.png")
    
    if not os.path.exists(idle_path) or not os.path.exists(walk_path):
        print(f"Skipping {char_name}: Idle or Walk spritesheet not found.")
        return
        
    idle_sheet = Image.open(idle_path).convert("RGBA")
    walk_sheet = Image.open(walk_path).convert("RGBA")
    
    # Helper to save a single frame with shadow and padding
    def save_frame(src_img, src_x, src_y, name):
        frame = src_img.crop((src_x, src_y, src_x+16, src_y+16))
        canvas = Image.new("RGBA", (32, 32), (0, 0, 0, 0))
        canvas.paste(shadow, (10, 15), shadow)
        canvas.paste(frame, (8, 8), frame)
        canvas.save(os.path.join(out_dir, name))

    # 1. Save Walk frames (Walk has 4 rows: Row 0 = Down, Row 1 = Up, Row 2 = Left, Row 3 = Right)
    directions = ["down", "up", "left", "right"]
    for d_idx, dir_name in enumerate(directions):
        for frame_idx in range(4):
            save_frame(walk_sheet, frame_idx * 16, d_idx * 16, f"walk_{dir_name}_0{frame_idx}.png")
            
    # 2. Save Idle Down frames (Idle.png has 4 frames for Down direction)
    for frame_idx in range(4):
        save_frame(idle_sheet, frame_idx * 16, 0, f"idle_down_0{frame_idx}.png")
        
    # 3. Generate Idle Up, Left, Right frames (by duplicating the first frame of the respective Walk direction)
    for frame_idx in range(4):
        save_frame(walk_sheet, 0, 1 * 16, f"idle_up_0{frame_idx}.png")     # Up Walk first frame
        save_frame(walk_sheet, 0, 2 * 16, f"idle_left_0{frame_idx}.png")   # Left Walk first frame
        save_frame(walk_sheet, 0, 3 * 16, f"idle_right_0{frame_idx}.png")  # Right Walk first frame

    print(f"Successfully processed {char_name} -> {out_folder_name}")

# Slice the chosen character assets
slice_npc("OldMan", "old_man")
slice_npc("Villager", "villager")
slice_npc("Woman", "woman")
slice_npc("Monk", "monk")
