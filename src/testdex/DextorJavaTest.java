package testdex;

import arc.*;
import arc.util.*;
import mindustry.*;
import mindustry.content.*;
import mindustry.game.EventType.*;
import mindustry.gen.*;
import mindustry.mod.*;
import mindustry.ui.dialogs.*;
import arc.Core;
import mindustry.Vars;
import mindustry.content.Items;
import mindustry.type.Category;
import mindustry.type.ItemStack;
import mindustry.world.meta.BuildVisibility;

import testdex.block.OverchargeDrill;

public class DextorJavaTest extends Mod{

    public DextorJavaTest(){
        Log.info("Loaded DextorJavaTest constructor.");

        //listen for game load event
        Events.on(ClientLoadEvent.class, e -> {
            //show dialog upon startup
            Time.runTask(10f, () -> {
                BaseDialog dialog = new BaseDialog("frog");
                dialog.cont.add("Dextor come this way").row();
                //mod sprites are prefixed with the mod name (this mod is called 'example-java-mod' in its config)
                dialog.cont.image(Core.atlas.find("dextor-java-test-dextor")).pad(20f).row();
                dialog.cont.button("yessir", dialog::hide).size(100f, 50f);
                dialog.show();
            });
        });
    }
    @Override
    public void loadContent() {
        OverchargeDrill customOverchargeDrill = new OverchargeDrill("overcharge-drill") {{
            requirements(Category.production, ItemStack.with());
            size = 3;
            drillTime = 600;
            maxCharge = 40;
            chargeChance = 0.5f;
        }};
        AccelTurret customAccelTurret = new AccelTurret("accel-turret"){{
            requirements(Category.turret, with());
            maxCharge = 15;
            ammo(
            Items.graphite, new BasicBulletType(8f, 41){{
                knockback = 4f;
                width = 25f;
                hitSize = 7f;
                height = 20f;
                shootEffect = Fx.shootBigColor;
                smokeEffect = Fx.shootSmokeSquareSparse;
                ammoMultiplier = 1;
                hitColor = backColor = trailColor = Color.valueOf("ea8878");
                frontColor = Pal.redLight;
                trailWidth = 6f;
                trailLength = 3;
                hitEffect = despawnEffect = Fx.hitSquaresColor;
                buildingDamageMultiplier = 0.2f;
            }},
            Items.oxide, new BasicBulletType(8f, 120){{
                knockback = 3f;
                width = 25f;
                hitSize = 7f;
                height = 20f;
                shootEffect = Fx.shootBigColor;
                smokeEffect = Fx.shootSmokeSquareSparse;
                ammoMultiplier = 2;
                hitColor = backColor = trailColor = Color.valueOf("a0b380");
                frontColor = Color.valueOf("e4ffd6");
                trailWidth = 6f;
                trailLength = 3;
                hitEffect = despawnEffect = Fx.hitSquaresColor;
                buildingDamageMultiplier = 0.2f;
            }},
            Items.silicon, new BasicBulletType(8f, 35){{
                knockback = 3f;
                width = 25f;
                hitSize = 7f;
                height = 20f;
                homingPower = 0.045f;
                shootEffect = Fx.shootBigColor;
                smokeEffect = Fx.shootSmokeSquareSparse;
                ammoMultiplier = 1;
                hitColor = backColor = trailColor = Pal.graphiteAmmoBack;
                frontColor = Pal.graphiteAmmoFront;
                trailWidth = 6f;
                trailLength = 6;
                hitEffect = despawnEffect = Fx.hitSquaresColor;
                buildingDamageMultiplier = 0.2f;
            }}
            );

            shoot = new ShootSpread(15, 4f);

            coolantMultiplier = 15f;

            inaccuracy = 0.2f;
            velocityRnd = 0.17f;
            shake = 1f;
            ammoPerShot = 3;
            maxAmmo = 30;
            consumeAmmoOnce = true;
            targetUnderBlocks = false;

            shootSound = Sounds.shootAltLong;

            drawer = new DrawTurret("reinforced-"){{
                parts.add(new RegionPart("-front"){{
                    progress = PartProgress.warmup;
                    moveRot = -10f;
                    mirror = true;
                    moves.add(new PartMove(PartProgress.recoil, 0f, -3f, -5f));
                    heatColor = Color.red;
                }});
            }};
            shootY = 5f;
            outlineColor = Pal.darkOutline;
            size = 3;
            envEnabled |= Env.space;
            reload = 30f;
            recoil = 2f;
            range = 125;
            shootCone = 40f;
            scaledHealth = 210;
            rotateSpeed = 3f;

            coolant = consume(new ConsumeLiquid(Liquids.water, 15f / 60f));
            limitRange(25f);
        }};
    }
}
