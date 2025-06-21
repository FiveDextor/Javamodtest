// File: Javamodtest/src/testdex/MyAwesomeMod.java

package testdex; // IMPORTANT: This package should match the directory it's in (src/testdex)

import arc.Core;
import mindustry.Vars;
import mindustry.content.Items;
import mindustry.mod.Mod;
import mindustry.type.Category;
import mindustry.type.ItemStack;
import mindustry.world.meta.BuildVisibility; // Added import for BuildVisibility

import testdex.block.OverchargeDrill; // IMPORTANT: Correct import path for your drill

public class MyAwesomeMod extends Mod {

    public MyAwesomeMod() {
        // Constructor
    }

    @Override
    public void loadContent() {
        Core.app.post(() -> { // Use post to ensure Vars.content is fully initialized
            // Instantiate your custom drill block
            OverchargeDrill customOverchargeDrill = new OverchargeDrill("overcharge-drill");

            // Set block properties directly on the instance (preferred way)
            customOverchargeDrill.size = 2; // Example size
            customOverchargeDrill.tier = 3; // Example tier
            customOverchargeDrill.drillTime = 180f; // Original drill time (e.g., 3 seconds per item)

            // Set your custom drill properties
            customOverchargeDrill.maxCharge = 100f; // Max charge
            customOverchargeDrill.chargeChance = 0.1f; // Set this to 0.1 for 1 in 10 chance, or 1.0 for guaranteed
            customOverchargeDrill.overchargeDuration = 450f; // 7.5 seconds
            customOverchargeDrill.cooldownDuration = 900f;  // 15 seconds

            // Set standard block properties
            customOverchargeDrill.requirements(
                new ItemStack(Items.lead, 100),
                new ItemStack(Items.titanium, 75),
                new ItemStack(Items.silicon, 50)
            );
            customOverchargeDrill.category = Category.production;
            customOverchargeDrill.buildVisibility = BuildVisibility.factory; // Make it visible in the factory build menu
            customOverchargeDrill.researchCostMultiplier = 0.5f;

            // This is crucial: Finalize the content and add it to the game's blocks list
            customOverchargeDrill.create(Vars.content.blocks());
        });
    }
}
