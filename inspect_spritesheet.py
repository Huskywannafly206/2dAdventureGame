from PIL import Image

player = Image.open("assets_raw/objects/player/idle_down_00.png").convert("RGBA")
shadow = Image.open("Ninja Adventure - Asset Pack/Actor/Character/Shadow.png").convert("RGBA")
ninja = Image.open("Ninja Adventure - Asset Pack/Actor/Character/NinjaGreen/SeparateAnim/Idle.png").convert("RGBA").crop((0, 0, 16, 16))

# Let's test different sy values to see which one has the minimum difference
min_diff = 9999
best_offset = (0, 0)
for sy in range(15, 22):
    for sx in range(8, 14):
        test_img = Image.new("RGBA", (32, 32), (0, 0, 0, 0))
        test_img.paste(shadow, (sx, sy), shadow)
        test_img.paste(ninja, (8, 8), ninja)
        
        diff_count = 0
        for y in range(32):
            for x in range(32):
                if test_img.getpixel((x, y)) != player.getpixel((x, y)):
                    diff_count += 1
        
        if diff_count < min_diff:
            min_diff = diff_count
            best_offset = (sx, sy)

print(f"Best offset: {best_offset} with {min_diff} differing pixels")
