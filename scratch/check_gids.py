import xml.etree.ElementTree as ET
import base64
import zlib
import struct

tree = ET.parse('assets/maps/village.tmx')
root = tree.getroot()

print("--- Objects ---")
for obj in root.findall(".//object"):
    name = obj.get("name", "")
    gid = obj.get("gid", "")
    x = obj.get("x", "")
    y = obj.get("y", "")
    props = {p.get("name"): p.get("value") for p in obj.findall(".//property")}
    print(f"ID={obj.get('id')} Name={name} GID={gid} X={x} Y={y} Props={props}")

print("\n--- Layers ---")
for layer in root.findall(".//layer"):
    name = layer.get("name")
    data_elem = layer.find("data")
    if data_elem is not None:
        encoding = data_elem.get("encoding")
        compression = data_elem.get("compression")
        if encoding == "base64" and compression == "zlib":
            data_bytes = base64.b64decode(data_elem.text.strip())
            decompressed = zlib.decompress(data_bytes)
            # 4 bytes per tile
            gids = [struct.unpack("<I", decompressed[i:i+4])[0] for i in range(0, len(decompressed), 4)]
            # filter out 0
            unique_gids = set(gids) - {0}
            print(f"Layer '{name}' has unique non-zero GIDs: {sorted(list(unique_gids))}")
