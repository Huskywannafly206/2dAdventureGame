<?xml version="1.0" encoding="UTF-8"?>
<tileset version="1.10" tiledversion="1.12.2" name="objects" tilewidth="80" tileheight="112" tilecount="7" columns="0">
 <grid orientation="orthogonal" width="1" height="1"/>
 <tile id="1" type="Object">
  <properties>
   <property name="animation" value="IDLE"/>
   <property name="animationSpeed" type="float" value="1"/>
   <property name="attackSound" value="SWING"/>
   <property name="damage" type="float" value="7"/>
   <property name="damageDelay" type="float" value="0.2"/>
   <property name="life" type="int" value="12"/>
   <property name="lifeReg" type="float" value="0.5"/>
   <property name="speed" type="float" value="3.5"/>
  </properties>
  <image source="objects/player.png" width="32" height="32"/>
  <objectgroup draworder="index" id="2">
   <object id="1" x="11" y="18" width="9" height="5">
    <ellipse/>
   </object>
   <object id="2" name="attack_sensor_down" x="0" y="17" width="32" height="17.6014">
    <properties>
     <property name="sensor" type="bool" value="true"/>
    </properties>
   </object>
   <object id="3" name="attack_sensor_up" x="0" y="-3.00156" width="32" height="18.0016">
    <properties>
     <property name="sensor" type="bool" value="true"/>
    </properties>
   </object>
   <object id="4" name="attack_sensor_left" x="-2.00104" y="0" width="17.001" height="32">
    <properties>
     <property name="sensor" type="bool" value="true"/>
    </properties>
   </object>
   <object id="5" name="attack_sensor_right" x="17" y="0" width="17.3345" height="32">
    <properties>
     <property name="sensor" type="bool" value="true"/>
    </properties>
   </object>
  </objectgroup>
 </tile>
 <tile id="2" type="Prop">
  <image source="objects/house.png" width="80" height="112"/>
  <objectgroup draworder="index" id="2">
   <object id="1" x="7" y="82" width="67" height="26"/>
  </objectgroup>
 </tile>
 <tile id="4" type="Prop">
  <image source="objects/chest.png" width="16" height="16"/>
  <objectgroup draworder="index" id="2">
   <object id="1" x="0" y="4" width="16" height="10"/>
  </objectgroup>
 </tile>
 <tile id="5" type="Prop">
  <image source="objects/oak_tree.png" width="41" height="63"/>
  <objectgroup draworder="index" id="2">
   <object id="1" x="13" y="54">
    <polygon points="0,0 6,1 11,1 16,-1 16,-2 14,-5 13,-13 3,-13 3,-6 2,-5 1,-3 0,-1"/>
   </object>
  </objectgroup>
 </tile>
 <tile id="6" type="Object">
  <properties>
   <property name="animation" value="IDLE"/>
   <property name="z" type="int" value="0"/>
  </properties>
  <image source="objects/trap.png" width="16" height="16"/>
 </tile>
 <tile id="7" type="Object">
  <properties>
   <property name="animation" value="IDLE"/>
   <property name="animationSpeed" type="float" value="1"/>
   <property name="bodyType" propertytype="BodyType" value="StaticBody"/>
   <property name="life" type="int" value="99999"/>
   <property name="lifeReg" type="float" value="9999"/>
  </properties>
  <image source="objects/training_dummy.png" width="32" height="32"/>
  <objectgroup draworder="index" id="2">
   <object id="1" x="3" y="12" width="26" height="16"/>
  </objectgroup>
 </tile>
 <tile id="9" type="Object">
  <properties>
   <property name="animation" value="IDLE"/>
   <property name="animationSpeed" type="float" value="1"/>
   <property name="attackRange" type="float" value="0.5"/>
   <property name="attackSound" value="SWING"/>
   <property name="bodyType" value="DynamicBody"/>
   <property name="damage" type="float" value="1"/>
   <property name="damageDelay" type="float" value="0.3"/>
   <property name="life" type="int" value="20"/>
   <property name="lifeReg" type="float" value="0.2"/>
   <property name="sightRange" type="float" value="10"/>
   <property name="speed" type="float" value="1.2"/>
   <property name="type" value="mob"/>
   <property name="xpReward" type="float" value="10"/>
  </properties>
  <image source="objects/slime.png" width="32" height="32"/>
  <objectgroup draworder="index" id="2">
   <object id="1" x="8" y="14" width="16" height="12">
    <ellipse/>
   </object>
   <object id="2" name="attack_sensor_down" x="4.13549" y="17" width="23.7957" height="10.8645">
    <properties>
     <property name="sensor" type="bool" value="true"/>
    </properties>
   </object>
   <object id="3" name="attack_sensor_up" x="3.80198" y="5.46952" width="24.0625" height="9.53048">
    <properties>
     <property name="sensor" type="bool" value="true"/>
    </properties>
   </object>
   <object id="4" name="attack_sensor_left" x="3.86868" y="5.33611" width="11.1313" height="22.5284">
    <properties>
     <property name="sensor" type="bool" value="true"/>
    </properties>
   </object>
   <object id="5" name="attack_sensor_right" x="17" y="5.33611" width="10.8645" height="22.5951">
    <properties>
     <property name="sensor" type="bool" value="true"/>
    </properties>
   </object>
  </objectgroup>
 </tile>
</tileset>
