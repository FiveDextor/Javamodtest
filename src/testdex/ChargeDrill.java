package testdex;

import arc.Core;
import mindustry.Vars;
import mindustry.content.Items;
import mindustry.mod.Mod;
import mindustry.type.Category;
import mindustry.type.ItemStack;

import your.mod.content.ChargeDrill; // Import your custom drill

public class MyAwesomeMod extends Mod {

    public MyAwesomeMod() {
        // Constructor
    }

    @Override
    public void loadContent() {
        Core.app.post(() -> {
            ChargeDrill customChargeDrill = new ChargeDrill("charge-drill") {{
                size = 2;
                tier = 3;
                drillTime = 180f; // Original drill time
                
                maxCharge = 100f; // Max charge
                chargeChance = 0.1f; // Set this to 0.1 for 1 in 10 chance, or 1.0 for guaranteed
                overchargeDuration = 450f;
                cooldownDuration = 900f;

                requirements(
                    new ItemStack(Items.lead, 100),
                    new ItemStack(Items.titanium, 75),
                    new ItemStack(Items.silicon, 50)
                );
                category = Category.production;
                researchCostMultiplier = 0.5f;
            }};
            
            customChargeDrill.create(Vars.content.blocks());
        });
    }
}
