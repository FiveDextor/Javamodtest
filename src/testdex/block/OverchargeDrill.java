package testdex;

import arc.graphics.Color;
import arc.struct.Seq;
import mindustry.content.Block;
import mindustry.content.Planets;
import mindustry.type.block.drill;

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
