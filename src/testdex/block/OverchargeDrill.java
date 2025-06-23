package testdex.block;

import mindustry.world.blocks.production.Drill;
import arc.math.Mathf;
import arc.Core;
import mindustry.ui.Bar;
import mindustry.Vars;
import arc.util.io.Writes;
import arc.util.io.Reads;
import arc.graphics.g2d.TextureRegion;
import mindustry.graphics.Pal;
import mindustry.graphics.Layer;
import arc.graphics.g2d.Draw;
import mindustry.graphics.Drawf;
import arc.util.Time;
import arc.graphics.Color;


public class OverchargeDrill extends Drill {
  // Max charge before the drill overcharged.
  public float maxCharge = 10;
  // Chance to gain charge per 1 items producing, 1 means always gain charge 0 = ... I don't need to tell that you know?
  public float chargeChance = 1;
  // Overcharged drills speed.
  public float overchargeMultiplier = 0.5f;
  // Overcharged time should be more than one for wind up speed.
  public float overchargeTime = 15 * 60;
  // Overcharge rotator wind up time.
  public float windUpTime = 60;
  // Overloaded time.
  public float overloadTime = 5 * 60;

  // Additional region for stuffs.
  public boolean drawCharge = true;
  public TextureRegion chargeRegion;
  // ONLY IF drawCharge is enabled.
  public Color chargeColor = Color.valueOf("ffffff");
  public Color overchargeColor = Color.valueOf("ff0000");
  public Color overloadColor = Color.valueOf("000000");
  
  public OverchargeDrill(String name) {
        super(name);
        size = 2;
  }
  public void load(){
        super.load();
            chargeRegion = Core.atlas.find(name + "-charge");
  }
  @Override
  public void setBars() {
      super.setBars();
      addBar("charge", (OverchargeDrillBuild e) ->
          new Bar(() -> Core.bundle.format("bar.charge", e.chargeSub), () -> Pal.ammo, () -> e.totalCharge / maxCharge));
  }
  public class OverchargeDrillBuild extends DrillBuild {
        public float totalCharge = 0;
        public boolean isOvercharging = false;
        public boolean isOverloading = false;
        public float overchargeTimeC = 0;
        public float overloadTimeC = 0;
        public float chargeSub = 0;
        public float windUp = 0;
    
        @Override
        public void write(Writes write){
          super.write(write);
          write.f(totalCharge);
          write.f(overchargeTimeC);
          write.f(overloadTimeC);
          write.f(chargeSub);
          write.f(windUp);
          write.bool(isOvercharging);
          write.bool(isOverloading);
        }
    
        @Override
        public void read(Reads read, byte revision){
          super.read(read, revision);
          totalCharge = read.f();
          overchargeTimeC = read.f();
          overloadTimeC = read.f();
          chargeSub = read.f();
          windUp = read.f();
          isOvercharging = read.bool();
          isOverloading = read.bool();
        }
        public TextureRegion[] icons(){
          return new TextureRegion[]{region, chargeRegion, rotatorRegion, topRegion};
        }
    
        @Override
        public void updateTile(){
            if(isOvercharging){
              if(windUp < windUpTime)windUp += 1;
            overchargeTimeC += 1;
            totalCharge = maxCharge * ((overchargeTime - overchargeTimeC) / overchargeTime);
            chargeSub = Mathf.ceil(maxCharge * ((overchargeTime - overchargeTimeC) / overchargeTime));
              if(overchargeTimeC > overchargeTime){
                 overchargeTimeC = 0;
                 chargeSub = 0;
                 totalCharge = 0;
                 windUp = 0;
                 isOvercharging = false;
                 isOverloading = true;
              }
            }
            if(isOverloading){
             overloadTimeC += 1;
              if(overloadTimeC > overloadTime){
                 overloadTimeC = 0;
                 isOverloading = false;
               }
             }
            if(timer(timerDump, dumpTime / timeScale)){
                dump(dominantItem != null && items.has(dominantItem) ? dominantItem : null);
            }

            if(dominantItem == null){
                return;
            }

            timeDrilled += warmup * delta();

            float delay = getDrillTime(dominantItem);
            if(isOvercharging)delay = delay * overchargeMultiplier;

            if(items.total() < itemCapacity && dominantItems > 0 && efficiency > 0 && isOverloading == false){
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
                if(Mathf.chance(chargeChance) && isOvercharging == false && totalCharge < maxCharge){
                    totalCharge += 1;
                    chargeSub = totalCharge;
                }else if (totalCharge >= maxCharge && isOvercharging == false){
                    isOvercharging = true;
                }

                progress %= delay;

                if(wasVisible && Mathf.chanceDelta(drillEffectChance * warmup)) drillEffect.at(x + Mathf.range(drillEffectRnd), y + Mathf.range(drillEffectRnd), dominantItem.color);
            }
        }
        @Override
        public void draw(){
            float s = 0.3f;
            float ts = 0.6f;

            Draw.rect(region, x, y);
            Draw.z(Layer.blockCracks);
            drawDefaultCracks();

            Draw.z(Layer.blockAfterCracks);
            if(drawRim){
                Draw.color(heatColor);
                Draw.alpha(warmup * ts * (1f - s + Mathf.absin(Time.time, 3f, s)));
                Draw.rect(rimRegion, x, y);
                Draw.color();
            }
            if(drawCharge){
                if(isOverloading)Draw.color(overloadColor);
                else if(isOvercharging)Draw.color(overchargeColor);
                else Draw.color(chargeColor);
                Draw.rect(chargeRegion, x, y);
                Draw.color();
            }

            if (drawSpinSprite) {
            if (isOvercharging) {
                Drawf.spinSprite(rotatorRegion, x, y,
                timeDrilled * rotateSpeed * (1 / overchargeMultiplier);
            } else {
                Drawf.spinSprite(rotatorRegion, x, y,
                timeDrilled * rotateSpeed);
            }
            } else {
                Draw.rect(rotatorRegion, x, y, timeDrilled * rotateSpeed);
            }

            Draw.rect(topRegion, x, y);

            if(dominantItem != null && drawMineItem){
                Draw.color(dominantItem.color);
                Draw.rect(itemRegion, x, y);
                Draw.color();
            }
        }
  }
}
