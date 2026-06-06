import xml.etree.ElementTree as ET
import base64
import zlib
import struct

def find_house_tiles():
    tree = ET.parse("assets/maps/village.tmx")
    root = tree.getroot()
    
    # Read firstgid of TilesetHouse.tsx
    house_firstgid = 0
    for tileset in root.findall('tileset'):
        source = tileset.get('source', '')
        if 'TilesetHouse' in source:
            house_firstgid = int(tileset.get('firstgid'))
            print(f"TilesetHouse firstgid: {house_firstgid}")
            break
            
    if not house_firstgid:
        print("TilesetHouse not found.")
        return

    # Find tile layers
    for layer in root.findall('layer'):
        name = layer.get('name')
        width = int(layer.get('width'))
        height = int(layer.get('height'))
        
        data_elem = layer.find('data')
        if data_elem is None:
            continue
            
        encoding = data_elem.get('encoding')
        compression = data_elem.get('compression')
        
        if encoding == 'base64' and compression == 'zlib':
            compressed_data = base64.b64decode(data_elem.text.strip())
            decompressed_data = zlib.decompress(compressed_data)
            # Unpack 32-bit unsigned integers (little endian)
            num_tiles = len(decompressed_data) // 4
            gids = struct.unpack(f'<{num_tiles}I', decompressed_data)
            
            # Look for GIDs belonging to TilesetHouse
            for idx, gid in enumerate(gids):
                if gid >= house_firstgid and gid < house_firstgid + 800: # estimate tileset size
                    x = idx % width
                    y = idx // width
                    # Convert to pixel coordinates (16x16 tiles)
                    px = x * 16
                    py = y * 16
                    # Let's print out some house tiles coordinates near player spawn (544, 152)
                    dist_to_spawn = ((px - 544)**2 + (py - 152)**2)**0.5
                    if dist_to_spawn < 150:
                        print(f"Layer '{name}' house tile GID={gid} at grid=({x},{y}), pixel=({px},{py}), dist={dist_to_spawn:.1f}")

if __name__ == '__main__':
    find_house_tiles()
