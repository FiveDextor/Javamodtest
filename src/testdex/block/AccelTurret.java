package testdex.block;

import mindustry.world.blocks.defense.turrets.ItemTurret;
import arc.math.Mathf;

public class AccelTurret extends ItemTurret {
  public float maxCharge = 60;
  public float multiplier = 1;
  
  public AccelTurret(String name) {
        super(name);
  }
  public class ItemTurretBuild extends TurretBuild{
  public float charge = 0;

  @Override
  protected void updateReload(){
            reloadCounter += delta() * ammoReloadMultiplier() * baseReloadSpeed();

            //cap reload for visual reasons
            reloadCounter = Math.min(reloadCounter,reload / (1 + multiplier * charge / maxCharge));
     }
  }

  @Override
  protected void updateShooting(){

            if(reloadCounter >= reload / (1 + multiplier * charge / maxCharge) && !charging() && shootWarmup >= minWarmup){
                BulletType type = peekAmmo();

                shoot(type);

                reloadCounter %= reload;
                charge += 1;
            }
  }
}
