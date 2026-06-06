from PIL import Image

# Load the dialog box background texture
img = Image.open("assets/ui/DialogBoxFaceset.png")
width, height = img.size
print(f"Image dimensions: {width}x{height}")

# Let's find the black slot. The slot is a dark area on the left.
# Let's print the pixels in a grid or find the bounding box of very dark pixels.
dark_pixels = []
for y in range(height):
    for x in range(width // 4): # only check the left quarter of the image
        r, g, b, a = img.getpixel((x, y))
        # The slot is black/very dark grey (e.g. RGB < 50) and opaque
        if r < 50 and g < 50 and b < 50 and a > 200:
            dark_pixels.append((x, y))

if dark_pixels:
    min_x = min(p[0] for p in dark_pixels)
    max_x = max(p[0] for p in dark_pixels)
    min_y = min(p[1] for p in dark_pixels)
    max_y = max(p[1] for p in dark_pixels)
    print(f"Dark slot bounding box in original image: x=[{min_x}, {max_x}], y=[{min_y}, {max_y}]")
    print(f"Slot size: {max_x - min_x + 1}x{max_y - min_y + 1}")
else:
    print("No dark slot found")
