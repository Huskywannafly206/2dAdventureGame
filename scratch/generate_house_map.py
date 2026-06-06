import base64
import zlib
import struct

def make_layer_data(width, height, value_func):
    data = bytearray()
    for y in range(height):
        for x in range(width):
            gid = value_func(x, y)
            data.extend(struct.pack('<I', gid))
    compressed = zlib.compress(data)
    return base64.b64encode(compressed).decode('utf-8')

def generate_house_tmx():
    width = 12
    height = 10
    
    # 1. Background layer: fill all with floor tile GID 573 (wood floor)
    bg_data = make_layer_data(width, height, lambda x, y: 573)
    
    # 2. Obstacle layer (walls):
    # Top wall (row 0, 1) and borders
    def obstacle_val(x, y):
        # Top-most border
        if y == 0:
            return 1187 # wall tile
        if y == 1:
            return 1219 # wall lower part
        # Side borders
        if x == 0 or x == width - 1:
            return 1187
        # Bottom border (with door gap in the middle)
        if y == height - 1:
            if x >= 5 and x <= 6:
                return 0 # door gap
            return 1187
        return 0
        
    obs_data = make_layer_data(width, height, obstacle_val)
    
    tmx_content = f"""<?xml version="1.0" encoding="UTF-8"?>
<map version="1.10" tiledversion="1.12.2" orientation="orthogonal" renderorder="right-down" width="{width}" height="{height}" tilewidth="16" tileheight="16" infinite="0" nextlayerid="6" nextobjectid="4">
 <tileset firstgid="1" source="TilesetFloor.tsx"/>
 <tileset firstgid="573" source="TilesetInteriorFloor.tsx"/>
 <tileset firstgid="947" source="TilesetElement.tsx"/>
 <tileset firstgid="1187" source="TilesetHouse.tsx"/>
 <tileset firstgid="1946" source="TilesetNature.tsx"/>
 <tileset firstgid="2450" source="Tree.tsx"/>
 <tileset firstgid="2454" source="objects.tsx"/>
 <tileset firstgid="2471" source="TilesetField.tsx"/>
 <tileset firstgid="2546" source="Combined3.tsx"/>
 <layer id="1" name="background" width="{width}" height="{height}">
  <data encoding="base64" compression="zlib">
   {bg_data}
  </data>
 </layer>
 <layer id="2" name="obstacle" width="{width}" height="{height}">
  <data encoding="base64" compression="zlib">
   {obs_data}
  </data>
 </layer>
 <objectgroup id="3" name="trigger">
  <object id="1" name="portal_trigger" x="80" y="144" width="32" height="16">
   <properties>
    <property name="targetMap" value="VILLAGE"/>
    <property name="targetX" type="float" value="496"/>
    <property name="targetY" type="float" value="176"/>
   </properties>
  </object>
 </objectgroup>
 <objectgroup id="4" name="objects">
  <object id="2" name="Player" gid="2455" x="96" y="96" width="32" height="32">
   <properties>
    <property name="camFollow" type="bool" value="true"/>
    <property name="controller" type="bool" value="true"/>
   </properties>
  </object>
  <object id="3" name="Old Man" gid="2464" x="64" y="64" width="32" height="32">
   <properties>
    <property name="npcName" value="Old Man"/>
   </properties>
  </object>
 </objectgroup>
</map>
"""
    with open("assets/maps/village_house.tmx", "w", encoding="utf-8") as f:
        f.write(tmx_content)
    print("Successfully generated assets/maps/village_house.tmx")

if __name__ == '__main__':
    generate_house_tmx()
