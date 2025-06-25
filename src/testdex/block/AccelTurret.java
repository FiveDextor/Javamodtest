package testdex.block;

import mindustry.world.blocks.defense.turrets.ItemTurret;
import mindustry.world.blocks.defense.turrets.Turret.*;
import arc.math.Mathf;
import mindustry.entities.bullet.BulletType;
import mindustry.ui.Bar;
import mindustry.ui.Styles;
import testdex.graphics.Pal;
import mindustry.world.meta.*;
import mindustry.world.consumers.ConsumeLiquidFilter;
import mindustry.type.Liquid;
import arc.Core;

import static mindustry.Vars.*;

public class AccelTurret extends ItemTurret {
    public float maxCharge = 60;
    public float multiplier = 1;
    public float chargeRemoveTime = 30;
    public float chargeRemoveAmount = 2;
    public float coolantIneffMultiplier = 1;

    public AccelTurret(String name) {
        super(name);
    }

    public void setBars() {
      super.setBars();
      addBar("charge", (AccelTurretBuild e) ->
          new Bar(() -> Core.bundle.format("bar.charge", e.charge), () -> Pal.ammo, () -> e.charge / maxCharge));
    }

    public class AccelTurretBuild extends ItemTurretBuild {
        public float charge = 0;
        public float chargeRemove = 0;

        protected float ammoReloadMultiplier(){
            return hasAmmo() ? peekAmmo().reloadMultiplier : 1f;
        }

        @Override
        protected void updateCooling(){
            if(reloadCounter < reload && coolant != null && coolant.efficiency(this) > 0 && efficiency > 0){
                float capacity = coolant instanceof ConsumeLiquidFilter filter
                ? filter.getConsumed(this).heatCapacity
                : liquids.current() != null && liquids.current().heatCapacity != 0 ? liquids.current().heatCapacity : 0.4f;             
                float amount = coolant.amount * coolant.efficiency(this);
                coolant.update(this);
                reloadCounter += amount * edelta() * capacity * coolantMultiplier * ammoReloadMultiplier() * (1 - (coolantIneffMultiplier - (charge / maxCharge)));

                if(Mathf.chance(0.06 * amount)){
                    coolEffect.at(x + Mathf.range(size * tilesize / 2f), y + Mathf.range(size * tilesize / 2f));
                }
            }
        }

        @Override
        public void updateTile(){
            if(!wasShooting && charge != 0){
                if(chargeRemove < chargeRemoveTime){
                    chargeRemove++;
                }else{
                    charge -= chargeRemoveAmount;
                    chargeRemove = 0;
                    if(charge < 0)charge = 0;
                }
            }else{
                chargeRemove = 0;
            }

            super.updateTile();
        }

        @Override
        protected void updateReload() {
            reloadCounter += delta() * ammoReloadMultiplier() * baseReloadSpeed();
            //cap reload for visual reasons
            reloadCounter = Math.min(reloadCounter, reload / (1 + (multiplier * charge / maxCharge)));
        }

        @Override
        protected void updateShooting() {
            if (reloadCounter >= reload / (1 + (multiplier * charge / maxCharge)) && !charging() && shootWarmup >= minWarmup) {
                BulletType type = peekAmmo();

                shoot(type);

                reloadCounter %= reload / (1 + (multiplier * charge / maxCharge));
                if(charge < maxCharge)charge += 1;
            }
        }
    }
}
