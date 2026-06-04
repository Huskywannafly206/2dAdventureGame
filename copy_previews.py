import shutil
import os

def copy_preview(src_folder, npc_name):
    src = f"assets_raw/objects/{src_folder}/idle_down_00.png"
    dest = f"assets/maps/objects/{npc_name}.png"
    shutil.copy(src, dest)
    print(f"Copied preview for {npc_name}")

copy_preview("old_man", "old_man")
copy_preview("villager", "villager")
copy_preview("woman", "woman")
copy_preview("monk", "monk")
