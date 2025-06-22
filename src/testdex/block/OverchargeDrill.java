package testdex.block;

import mindustry.world.blocks.production.Drill;
import arc.math.Mathf;
import mindustry.ui.Bar;
import mindustry.graphics.Pal;
import mindustry.Vars;


public class OverchargeDrill extends Drill {
  // Max charge before the drill overcharged.
  public float maxCharge = 10;
  // Chance to gain charge per 1 items producing, 1 means always gain charge 0 = ... I don't need to tell that you know?
  public float chargeChance = 1;

  public OverchargeDrill(String name) {
        super(name);
        size = 2;
  }

  public class OverchargeDrillBuild extends DrillBuild {
        public float totalCharge = 0;
        public boolean isOvercharged = false;
        @Override
        public void setBars(){
            // super.setBars();

            addBar("overcharge", (OverchargeDrillBuild e) ->
             new Bar(() -> Core.bundle.format("bar.drillspeed", e.totalCharge), () -> Pal.ammo, () -> e.totalCharge/maxCharge));
        }
        @Override
        public void updateTile(){
            if(timer(timerDump, dumpTime / timeScale)){
                dump(dominantItem != null && items.has(dominantItem) ? dominantItem : null);
            }

            if(dominantItem == null){
                return;
            }

            timeDrilled += warmup * delta();

            float delay = getDrillTime(dominantItem);

            if(items.total() < itemCapacity && dominantItems > 0 && efficiency > 0){
                float speed = Mathf.lerp(1f, liquidBoostIntensity, optionalEfficiency) * efficiency;

                lastDrillSpeed = (speed * dominantItems * warmup) / delay;
                warmup = Mathf.approachDelta(warmup, speed, warmupSpeed);
                progress += delta() * dominantItems * speed * warmup;

                if(Mathf.chanceDelta(updateEffectChance * warmup))
                    updateEffect.at(x + Mathf.range(size * 2f), y + Mathf.range(size * 2f));
            }else{
                lastDrillSpeed = 0f;
                warmup = Mathf.approachDelta(warmup, 0f, warmupSpeed);
                return;
            }

            // This is the check for if we have drilled enough to produce item
            if(dominantItems > 0 && progress >= delay && items.total() < itemCapacity){
                int amount = (int)(progress / delay);

                // This is when we produce an item
                for(int i = 0; i < amount; i++){
                    offload(dominantItem);
                }
                // Charge gaining and Overcharging
                if(Mathf.chance(chargeChance) && isOvercharged == false && totalCharge < maxCharge){
                    totalCharge += 1;
                }else if (totalCharge >= maxCharge && isOvercharged == false){
                    isOvercharged = true;
                }

                progress %= delay;

                if(wasVisible && Mathf.chanceDelta(drillEffectChance * warmup)) drillEffect.at(x + Mathf.range(drillEffectRnd), y + Mathf.range(drillEffectRnd), dominantItem.color);
            }
        }
  }
}
