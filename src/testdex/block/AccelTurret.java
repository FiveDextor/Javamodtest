package testdex.block;

import mindustry.world.blocks.defense.turrets.ItemTurret;

public class AccelTurret extends ItemTurret {
  public AccelTurret(String name) {
        super(name);
  }
  public class ItemTurretBuild extends TurretBuild{
    public float charge = 0;
    public float maxCharge = 60;
    public float multiplier = 2;
  }
}
