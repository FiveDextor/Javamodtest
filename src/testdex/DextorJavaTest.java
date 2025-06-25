package testdex;

import arc.*;
import arc.util.*;
import mindustry.*;
import mindustry.content.*;
import mindustry.game.EventType.*;
import mindustry.gen.*;
import mindustry.mod.*;
import mindustry.ui.dialogs.*;
import mindustry.entities.bullet.*;
import arc.Core;
import arc.graphics.Color;
import mindustry.Vars;
import mindustry.content.Items;
import mindustry.type.Category;
import mindustry.type.ItemStack;
import mindustry.entities.pattern.*;
import mindustry.entities.part.DrawPart.*;
import mindustry.entities.part.*;
import mindustry.entities.pattern.*;
import mindustry.graphics.Pal;
import mindustry.entities.effect.*;

import testdex.block.OverchargeDrill;
import testdex.block.AccelTurret;

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
            requirements(Category.turret, ItemStack.with());
            maxCharge = 15;
            ammo(
            Items.graphite, new BasicBulletType(8f, 41){{
                knockback = 4f;
                width = 25f;
                hitSize = 7f;
                height = 20f;
                ammoMultiplier = 1;
                hitColor = backColor = trailColor = Color.valueOf("ea8878");
                frontColor = Color.valueOf("ff0000");
                trailWidth = 6f;
                trailLength = 3;
                buildingDamageMultiplier = 0.2f;
            }},
            Items.oxide, new BasicBulletType(8f, 120){{
                knockback = 3f;
                width = 25f;
                hitSize = 7f;
                height = 20f;
                ammoMultiplier = 2;
                hitColor = backColor = trailColor = Color.valueOf("a0b380");
                frontColor = Color.valueOf("e4ffd6");
                trailWidth = 6f;
                trailLength = 3;
                buildingDamageMultiplier = 0.2f;
            }},
            Items.silicon, new BasicBulletType(8f, 35){{
                knockback = 3f;
                width = 25f;
                hitSize = 7f;
                height = 20f;
                homingPower = 0.045f;
                ammoMultiplier = 1;
                hitColor = backColor = trailColor = Color.valueOf("a0b380");
                frontColor = Color.valueOf("e4ffd6");
                trailWidth = 6f;
                trailLength = 6;
                buildingDamageMultiplier = 0.2f;
            }}
            );

            shoot = new ShootSpread(15, 4f);

            coolantMultiplier = 15f;
            
            maxCharge = 30;
            multiplier = 4;
            inaccuracy = 0.2f;
            velocityRnd = 0.17f;
            shake = 1f;
            ammoPerShot = 3;
            maxAmmo = 30;
            consumeAmmoOnce = true;
            targetUnderBlocks = false;

            shootY = 5f;
            size = 3;
            reload = 30f;
            recoil = 2f;
            range = 125;
            shootCone = 40f;
            scaledHealth = 210;
            rotateSpeed = 3f;
        }};
    }
}
