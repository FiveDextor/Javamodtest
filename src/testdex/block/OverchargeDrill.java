package testdex.content;

import arc.graphics.Color;
import arc.struct.Seq;
import mindustry.content.Block;
import mindustry.content.Planets;
import mindustry.world.blocks.production.Drill;
import mindustry.content;
import mindustry.entities;
import mindustry.gen;
import mindustry.graphics;
import mindustry.type;
import mindustry.world.consumers;
import mindustry.world.meta;

public class OverchargeDrill extends Drill{
  // Max charge of drill before overcharing
  public float maxCharge = 10;
  // Chance to gain charge from producing an item
  public float chargeChance = 1;
  public float overchargeDrillTime = 150;
  public float overchargeTime = 6000;
  public float overchargeRecover = 3000;
  public Effect overchargeEffect = Fx.smoke;
}
