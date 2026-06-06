<?xml version="1.0" encoding="UTF-8"?>
<tileset version="1.10" tiledversion="1.12.2" name="objects" tilewidth="96" tileheight="112" tilecount="23" columns="0">
 <grid orientation="orthogonal" width="1" height="1"/>
 <tile id="1" type="Object">
  <properties>
   <property name="animation" value="IDLE"/>
   <property name="animationSpeed" type="float" value="2"/>
   <property name="attackSound" value="SWING"/>
   <property name="damage" type="float" value="100"/>
   <property name="damageDelay" type="float" value="0.2"/>
   <property name="life" type="int" value="20"/>
   <property name="lifeReg" type="float" value="0.5"/>
   <property name="speed" type="float" value="10"/>
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
  <properties>
   <property name="sortOffsetY" type="int" value="-82"/>
  </properties>
  <image source="objects/house.png" width="80" height="112"/>
  <objectgroup draworder="index" id="2">
   <object id="1" x="7" y="82" width="67" height="26"/>
  </objectgroup>
 </tile>
 <tile id="4" type="Prop">
  <image source="objects/chest.png" width="32" height="16"/>
  <objectgroup draworder="index" id="2">
   <object id="1" x="0" y="4" width="16" height="10"/>
  </objectgroup>
 </tile>
 <tile id="5" type="Prop">
  <properties>
   <property name="sortOffsetY" type="int" value="-54"/>
  </properties>
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
   <property name="bodyType" value="StaticBody"/>
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
   <property name="attackRange" type="float" value="1.2"/>
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
 <tile id="10" type="Object">
  <properties>
   <property name="animation" value="IDLE"/>
   <property name="animationSpeed" type="float" value="1"/>
   <property name="bodyType" value="StaticBody"/>
  </properties>
  <image source="objects/old_man.png" width="32" height="32"/>
  <objectgroup draworder="index" id="2">
   <object id="1" x="11" y="18" width="9" height="5">
    <ellipse/>
   </object>
  </objectgroup>
 </tile>
 <tile id="11" type="Object">
  <properties>
   <property name="animation" value="IDLE"/>
   <property name="animationSpeed" type="float" value="1"/>
   <property name="bodyType" value="StaticBody"/>
  </properties>
  <image source="objects/villager.png" width="32" height="32"/>
  <objectgroup draworder="index" id="2">
   <object id="1" x="11" y="18" width="9" height="5">
    <ellipse/>
   </object>
  </objectgroup>
 </tile>
 <tile id="12" type="Object">
  <properties>
   <property name="animation" value="IDLE"/>
   <property name="animationSpeed" type="float" value="1"/>
   <property name="bodyType" value="StaticBody"/>
  </properties>
  <image source="objects/woman.png" width="32" height="32"/>
  <objectgroup draworder="index" id="2">
   <object id="1" x="11" y="18" width="9" height="5">
    <ellipse/>
   </object>
  </objectgroup>
 </tile>
 <tile id="13" type="Object">
  <properties>
   <property name="animation" value="IDLE"/>
   <property name="animationSpeed" type="float" value="1"/>
   <property name="bodyType" value="StaticBody"/>
  </properties>
  <image source="objects/monk.png" width="32" height="32"/>
  <objectgroup draworder="index" id="2">
   <object id="1" x="11" y="18" width="9" height="5">
    <ellipse/>
   </object>
  </objectgroup>
 </tile>
 <tile id="14" type="Object">
  <properties>
   <property name="animation" value="IDLE"/>
   <property name="animationSpeed" type="float" value="1"/>
   <property name="bodyType" value="StaticBody"/>
   <property name="dialogue" value="[ITEM]Hello! I am a fighter.|[DEAD]I am so tired...|[IDLE]Let's go train!"/>
   <property name="faceset" value="ui/fighter_white_faceset.png"/>
   <property name="npcName" value="fighter_white"/>
  </properties>
  <image source="objects/fighter_white.png" width="32" height="32"/>
  <objectgroup draworder="index" id="2">
   <object id="1" x="11" y="18" width="9" height="5">
    <ellipse/>
   </object>
  </objectgroup>
 </tile>
 <tile id="15" type="Object">
  <properties>
   <property name="animation" value="IDLE"/>
   <property name="animationSpeed" type="float" value="0.625"/>
   <property name="bodyType" value="StaticBody"/>
   <property name="life" type="int" value="200"/>
   <property name="maxLife" type="int" value="200"/>
  </properties>
  <image source="objects/green_tower.png" width="32" height="32"/>
  <objectgroup draworder="index" id="2">
   <object id="1" x="4" y="16" width="24" height="16"/>
  </objectgroup>
 </tile>
 <tile id="16" type="Object">
  <properties>
   <property name="animation" value="IDLE"/>
   <property name="animationSpeed" type="float" value="0.67"/>
   <property name="atlasAsset" value="OBJECTS"/>
   <property name="attackRange" type="float" value="3"/>
   <property name="attackSound" value="SWING"/>
   <property name="bodyType" value="DynamicBody"/>
   <property name="damage" type="float" value="0.3"/>
   <property name="damageDelay" type="float" value="0.45"/>
   <property name="life" type="int" value="800"/>
   <property name="lifeReg" type="float" value="0.2"/>
   <property name="sightRange" type="float" value="30"/>
   <property name="speed" type="float" value="1.5"/>
   <property name="type" value="mob"/>
   <property name="xpReward" type="float" value="10"/>
  </properties>
  <image source="objects/GiantBlueSamurai_Idle_00.png" width="96" height="48"/>
  <objectgroup draworder="index" id="2">
   <object id="1" x="24" y="21" width="48" height="18">
    <ellipse/>
   </object>
   <object id="2" name="attack_sensor_down" x="4.65" y="28.75" width="89.9" height="32.55">
    <properties>
     <property name="sensor" type="bool" value="true"/>
    </properties>
   </object>
   <object id="3" name="attack_sensor_up" x="11.4" y="-12.8" width="72.2" height="35.3">
    <properties>
     <property name="sensor" type="bool" value="true"/>
    </properties>
   </object>
   <object id="4" name="attack_sensor_left" x="-3.4" y="-4.5" width="47.9" height="55.8">
    <properties>
     <property name="sensor" type="bool" value="true"/>
    </properties>
   </object>
   <object id="5" name="attack_sensor_right" x="52" y="-3.25" width="45.1" height="54.9">
    <properties>
     <property name="sensor" type="bool" value="true"/>
    </properties>
   </object>
  </objectgroup>
 </tile>
 <tile id="17" type="Object">
  <properties>
   <property name="bodyType" value="StaticBody"/>
   <property name="hiddenBarrier" type="bool" value="true"/>
  </properties>
  <image source="objects/stone_block.png" width="16" height="16"/>
  <objectgroup draworder="index" id="2">
   <object id="1" x="0" y="0" width="16" height="16"/>
  </objectgroup>
 </tile>
 <tile id="18" type="Object">
  <properties>
   <property name="bodyType" value="StaticBody"/>
   <property name="npcName" value="Heart Container"/>
  </properties>
  <image source="objects/heart_container.png" width="16" height="16"/>
  <objectgroup draworder="index" id="2">
   <object id="1" x="0" y="0" width="16" height="16"/>
  </objectgroup>
 </tile>
 <tile id="19" type="Object">
  <properties>
   <property name="animation" value="IDLE"/>
   <property name="animationSpeed" type="float" value="1"/>
   <property name="attackRange" type="float" value="1.2"/>
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
  <image source="objects/spirit.png" width="16" height="16"/>
  <objectgroup draworder="index" id="2">
   <object id="1" x="4" y="7" width="8" height="6">
    <ellipse/>
   </object>
   <object id="2" name="attack_sensor_down" x="2" y="8" width="12" height="6">
    <properties>
     <property name="sensor" type="bool" value="true"/>
    </properties>
   </object>
   <object id="3" name="attack_sensor_up" x="2" y="2" width="12" height="6">
    <properties>
     <property name="sensor" type="bool" value="true"/>
    </properties>
   </object>
   <object id="4" name="attack_sensor_left" x="0" y="2" width="6" height="12">
    <properties>
     <property name="sensor" type="bool" value="true"/>
    </properties>
   </object>
   <object id="5" name="attack_sensor_right" x="10" y="2" width="6" height="12">
    <properties>
     <property name="sensor" type="bool" value="true"/>
    </properties>
   </object>
  </objectgroup>
 </tile>
 <tile id="20" type="Object">
  <properties>
   <property name="animation" value="IDLE"/>
   <property name="animationSpeed" type="float" value="1"/>
   <property name="attackRange" type="float" value="1.2"/>
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
  <image source="objects/skull.png" width="16" height="16"/>
  <objectgroup draworder="index" id="2">
   <object id="1" x="4" y="7" width="8" height="6">
    <ellipse/>
   </object>
   <object id="2" name="attack_sensor_down" x="2" y="8" width="12" height="6">
    <properties>
     <property name="sensor" type="bool" value="true"/>
    </properties>
   </object>
   <object id="3" name="attack_sensor_up" x="2" y="2" width="12" height="6">
    <properties>
     <property name="sensor" type="bool" value="true"/>
    </properties>
   </object>
   <object id="4" name="attack_sensor_left" x="0" y="2" width="6" height="12">
    <properties>
     <property name="sensor" type="bool" value="true"/>
    </properties>
   </object>
   <object id="5" name="attack_sensor_right" x="10" y="2" width="6" height="12">
    <properties>
     <property name="sensor" type="bool" value="true"/>
    </properties>
   </object>
  </objectgroup>
 </tile>
 <tile id="21" type="Object">
  <properties>
   <property name="bodyType" value="StaticBody"/>
   <property name="npcName" value="Gold Key"/>
  </properties>
  <image source="objects/gold_key.png" width="16" height="16"/>
  <objectgroup draworder="index" id="2">
   <object id="1" x="0" y="0" width="16" height="16"/>
  </objectgroup>
 </tile>
 <tile id="22" type="Object">
  <properties>
   <property name="bodyType" value="StaticBody"/>
   <property name="npcName" value="Silver Key"/>
  </properties>
  <image source="objects/silver_key.png" width="16" height="16"/>
  <objectgroup draworder="index" id="2">
   <object id="1" x="0" y="0" width="16" height="16"/>
  </objectgroup>
 </tile>
 <tile id="23" type="Object">
  <properties>
   <property name="bodyType" value="StaticBody"/>
   <property name="itemType" value="WEAPON_BOW"/>
  </properties>
  <image source="objects/weapon_bow.png" width="15" height="7"/>
  <objectgroup draworder="index" id="2">
   <object id="1" x="0" y="0" width="15" height="7">
    <properties>
     <property name="sensor" type="bool" value="true"/>
    </properties>
   </object>
  </objectgroup>
 </tile>
 <tile id="24" type="Object">
  <properties>
   <property name="bodyType" value="StaticBody"/>
   <property name="itemType" value="WEAPON_MAGIC_WAND"/>
  </properties>
  <image source="objects/weapon_magic_wand.png" width="5" height="18"/>
  <objectgroup draworder="index" id="2">
   <object id="1" x="0" y="0" width="5" height="18">
    <properties>
     <property name="sensor" type="bool" value="true"/>
    </properties>
   </object>
  </objectgroup>
 </tile>
 <tile id="25" type="Object">
  <properties>
   <property name="bodyType" value="StaticBody"/>
   <property name="itemType" value="WEAPON_SWORD"/>
  </properties>
  <image source="objects/weapon_sword.png" width="6" height="15"/>
  <objectgroup draworder="index" id="2">
   <object id="1" x="0" y="0" width="6" height="15">
    <properties>
     <property name="sensor" type="bool" value="true"/>
    </properties>
   </object>
  </objectgroup>
 </tile>
</tileset>
