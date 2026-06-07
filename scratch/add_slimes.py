import xml.etree.ElementTree as ET

tree = ET.parse('assets/maps/village.tmx')
root = tree.getroot()

# Find objectgroup named "objects"
objects_group = None
for og in root.findall(".//objectgroup"):
    if og.get("name") == "objects":
        objects_group = og
        break

if objects_group is not None:
    # Check if there are already slimes (GID 2463)
    slimes = [obj for obj in objects_group.findall("object") if obj.get("gid") == "2463"]
    print(f"Current slimes in map: {len(slimes)}")
    
    if len(slimes) < 4:
        # We need to add slimes to make it 4
        needed = 4 - len(slimes)
        print(f"Adding {needed} slimes...")
        next_id = int(root.get("nextobjectid", "106"))
        
        slime_coords = [
            (450.0, 550.0),
            (500.0, 530.0),
            (780.0, 560.0),
            (820.0, 540.0)
        ]
        
        for i in range(needed):
            x, y = slime_coords[i % len(slime_coords)]
            # offset slightly to avoid exact overlap if we need to add multiple at same coord
            if i >= len(slime_coords):
                x += 16.0
                y += 16.0
            
            elem = ET.Element("object", {
                "id": str(next_id),
                "gid": "2463",
                "x": str(x),
                "y": str(y),
                "width": "32",
                "height": "32"
            })
            objects_group.append(elem)
            next_id += 1
            
        root.set("nextobjectid", str(next_id))
        tree.write('assets/maps/village.tmx', encoding='UTF-8', xml_declaration=True)
        print("Successfully updated village.tmx with slimes.")
else:
    print("Could not find 'objects' group in village.tmx")
