package testdex; // This package declaration MUST match the directory: Javamodtest/src/testdex/

import arc.Core;
import mindustry.Vars;
import mindustry.content.Items;
import mindustry.mod.Mod;
import mindustry.type.Category; // Correct import for Category
import mindustry.type.ItemStack;
import mindustry.world.meta.BuildVisibility; // Correct import for BuildVisibility

import testdex.block.OverchargeDrill; // This import MUST match the package of OverchargeDrill.java

public class MyAwesomeMod extends Mod {

    public MyAwesomeMod() {
        // Constructor, usually empty for content mods
    }

    @Override
    public void loadContent() {
        // Core.app.post() is crucial here to ensure Mindustry's content systems are fully initialized
        // before your mod attempts to add new blocks.
        Core.app.post(() -> {
            // Instantiate your custom drill block.
            // Mindustry's content loading system will automatically discover this block
            // because it's instantiated in the loadContent() method of your Mod class.
            OverchargeDrill customOverchargeDrill = new OverchargeDrill("overcharge-drill");

            // --- Set Block Properties ---
            // Set basic block properties directly on the instance
            customOverchargeDrill.size = 2; // Sets the size of the block (e.g., 2x2 tiles)
            customOverchargeDrill.tier = 3; // Sets the mining tier (what ores it can mine)
            customOverchargeDrill.drillTime = 180f; // Base drill time in ticks (60 ticks = 1 second)

            // Set custom properties defined in OverchargeDrill
            customOverchargeDrill.maxCharge = 100f;
            customOverchargeDrill.chargeChance = 0.1f; // 0.1 for 1 in 10 chance, 1.0 for guaranteed
            customOverchargeDrill.overchargeDuration = 450f; // Overcharge duration in ticks (e.g., 7.5 seconds)
            customOverchargeDrill.cooldownDuration = 900f;  // Cooldown duration in ticks (e.g., 15 seconds)

            // Define the resources required to build the drill.
            // The requirements() method takes Category and BuildVisibility, then the ItemStacks.
            customOverchargeDrill.requirements(
                Category.production,        // Sets the tech tree category
                BuildVisibility.factory,    // Makes it visible in the factory build menu
                new ItemStack(Items.lead, 100),
                new ItemStack(Items.titanium, 75),
                new ItemStack(Items.silicon, 50)
            );

            // Other properties
            customOverchargeDrill.researchCostMultiplier = 0.5f; // Adjusts research cost
        });
    }
}
