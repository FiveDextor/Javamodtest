package testdex.block;

import mindustry.world.blocks.defense.turrets.ItemTurret;

public class AccelTurret extends ItemTurret {
  public float maxCharge = 60;
  public float multiplier = 2;
  
  public AccelTurret(String name) {
        super(name);
  }
  public class ItemTurretBuild extends TurretBuild{
  public float charge = 0;

  @Override
  protected void updateCooling(){
    float boostCharge = charge / maxCharge;
            if(reloadCounter < reload && coolant != null && coolant.efficiency(this) > 0 && efficiency > 0){
                float capacity = coolant instanceof ConsumeLiquidFilter filter ? filter.getConsumed(this).heatCapacity : (coolant.consumes(liquids.current()) ? liquids.current().heatCapacity : 0.4f);
                float amount = coolant.amount * coolant.efficiency(this);
                coolant.update(this);
                reloadCounter += amount * edelta() * capacity * coolantMultiplier * ammoReloadMultiplier();

                if(Mathf.chance(0.06 * amount)){
                    coolEffect.at(x + Mathf.range(size * tilesize / 2f), y + Mathf.range(size * tilesize / 2f));
                }
            }
        }
  }
}
