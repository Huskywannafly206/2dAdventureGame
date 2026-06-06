import xml.etree.ElementTree as ET

def add_door():
    path = "assets/maps/village.tmx"
    # Parse keeping format intact
    tree = ET.parse(path)
    root = tree.getroot()
    
    # Find the trigger objectgroup
    trigger_group = None
    for og in root.findall('objectgroup'):
        if og.get('name') == 'trigger':
            trigger_group = og
            break
            
    if trigger_group is None:
        print("Trigger group not found!")
        return

    # Check if we already added it
    for obj in trigger_group.findall('object'):
        props = obj.find('properties')
        if props is not None:
            for prop in props.findall('property'):
                if prop.get('name') == 'targetMap' and prop.get('value') == 'VILLAGE_HOUSE':
                    print("Door trigger already exists in village.tmx")
                    return

    # Create new object element
    obj = ET.Element('object', {
        'id': '90',
        'name': 'portal_trigger',
        'x': '464',
        'y': '144',
        'width': '32',
        'height': '16'
    })
    
    props = ET.Element('properties')
    prop_map = ET.Element('property', {'name': 'targetMap', 'value': 'VILLAGE_HOUSE'})
    prop_tx = ET.Element('property', {'name': 'targetX', 'type': 'float', 'value': '96'})
    prop_ty = ET.Element('property', {'name': 'targetY', 'type': 'float', 'value': '112'})
    
    props.append(prop_map)
    props.append(prop_tx)
    props.append(prop_ty)
    obj.append(props)
    
    trigger_group.append(obj)
    
    # Write back
    tree.write(path, encoding="utf-8", xml_declaration=True)
    print("Successfully added door trigger to village.tmx")

if __name__ == '__main__':
    add_door()
