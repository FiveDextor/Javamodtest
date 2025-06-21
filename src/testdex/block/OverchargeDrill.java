package testdex.block; // IMPORTANT: Ensure this matches your directory structure

import arc.Core;
import arc.graphics.Color;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.Fill;
import arc.math.Mathf;
import mindustry.Vars;
import mindustry.content.Fx;
import mindustry.entities.Effect;
import mindustry.gen.Call;
import mindustry.gen.Sounds;
import mindustry.world.Block;
import mindustry.world.blocks.production.Drill;
import mindustry.world.meta.Stat;
import mindustry.world.meta.StatUnit;
import mindustry.world.meta.BuildVisibility;
import mindustry.type.Category; // Added import for Category

public class OverchargeDrill extends Drill {

    // --- Configurable Drill Properties ---
    public float maxCharge = 100f; // Max charge capacity
    public float chargeChance = 0.5f; // Chance (0.0 to 1.0) to gain 1 charge per item produced
                                      // 1.0 = guaranteed, 0.1 = 1 in 10 chance
    public float overchargeDuration = 300f; // Duration of overcharge in ticks (e.g., 5 seconds * 60 ticks/sec)
    public float overchargeDrillTimeMultiplier = 0.5f; // New drillTime = original drillTime * multiplier (e.g., 0.5 for 2x faster)
    public float cooldownDuration = 600f; // Duration of cooldown in ticks (e.g., 10 seconds)
    public Effect overchargeEffect = Fx.hitLiquid; // Effect when entering overcharge
    public Color chargeBarColor = Color.cyan;
    public Color overchargeBarColor = Color.red;

    public OverchargeDrill(String name) {
        super(name);
        update = true; // Essential: allows updateTile() to be called
        // Default properties that should be set during block definition
        // These can be overridden in your mod's main class during initialization
        // if you want different values for specific instances of the drill.
        // For example:
        // this.size = 2;
        // this.tier = 3;
        // this.drillTime = 180f; // Base drill time
        // this.category = Category.production;
        // this.buildVisibility = BuildVisibility.factory;
    }

    // --- Stats for Display (Optional but good practice) ---
    @Override
    public void setStats() {
        super.setStats(); // Call super method to include base drill stats
        stats.add(Stat.charge, maxCharge);
        stats.add(Stat.boost, (int)(chargeChance * 100) + "%", StatUnit.percent); // Display chance as percentage
        stats.add(Stat.burstDamage, overchargeDuration / 60f, StatUnit.seconds); // Display overcharge duration
        stats.add(Stat.cooldown, cooldownDuration / 60f, StatUnit.seconds); // Display cooldown duration
        // Correctly show actual overcharge drill speed relative to original
        stats.add(Stat.drillSpeed, ((1f / (this.drillTime * overchargeDrillTimeMultiplier)) * 60f), StatUnit.itemsSecond);
    }

    // --- Custom Tile Entity for State Management ---
    public class OverchargeDrillBuild extends DrillBuild {
        public float currentCharge = 0f;
        public float overchargeTimer = 0f;
        public float cooldownTimer = 0f;
        public boolean isOvercharged = false;
        public boolean inCooldown = false;

        @Override
        public void updateTile() {
            if (inCooldown) {
                cooldownTimer -= Vars.delta;
                if (cooldownTimer <= 0) {
                    inCooldown = false;
                    // Reset to normal drillTime
                    drillTime = OverchargeDrill.this.drillTime; // Use outer class's original drillTime
                    currentCharge = 0f; // Reset charge after cooldown
                    Fx.reactorExplosion.at(x, y, 0f, Color.green); // Visual cue for returning to normal
                    if (Vars.net.client()) {
                        Sounds.blockRevive.at(x, y);
                    }
                }
                return; // Stop processing if in cooldown
            }

            if (isOvercharged) {
                overchargeTimer -= Vars.delta;
                if (overchargeTimer <= 0) {
                    // Overcharge ends, enter cooldown
                    isOvercharged = false;
                    inCooldown = true;
                    cooldownTimer = cooldownDuration;
                    Fx.vaporize.at(x, y, 0f, Color.red); // Visual cue for entering cooldown
                    if (Vars.net.client()) {
                        Sounds.explosion.at(x, y);
                    }
                    return; // Stop processing this tick, cooldown has begun
                }
                // During overcharge, drill at faster rate (drillTime is already set)
                super.updateTile();
                return; // Stop further processing if overcharged
            }

            // Normal operation (not overcharged, not in cooldown)
            super.updateTile(); // Call the base Drill's updateTile for normal mining logic
        }

        // --- Overriding mineItem to add charge gain with a chance ---
        @Override
        protected void mineItem(Block block, int amount) {
            super.mineItem(block, amount); // Let the base drill do its job first

            // Gain charge for each item produced with a chance (if not already full or in cooldown)
            if (!isOvercharged && !inCooldown) {
                // Loop 'amount' times, as 'amount' items might have been produced in one go
                for (int i = 0; i < amount; i++) {
                    if (Mathf.random() < OverchargeDrill.this.chargeChance) { // Use outer class's chargeChance
                        currentCharge = Mathf.clamp(currentCharge + 1f, 0, maxCharge); // Gain exactly 1 charge

                        if (currentCharge >= maxCharge) {
                            // Full charge, activate overcharge!
                            isOvercharged = true;
                            overchargeTimer = overchargeDuration;
                            currentCharge = maxCharge; // Ensure it stays at max

                            // Set drillTime to be faster during overcharge
                            drillTime = OverchargeDrill.this.drillTime * overchargeDrillTimeMultiplier; // Use outer class's drillTime

                            // Effects and sounds for overcharge activation
                            overchargeEffect.at(x, y);
                            if (Vars.net.client()) { // Only play sounds/effects on client side
                                Sounds.laserbig.at(x, y);
                                Call.effect(Fx.reactorsmoke, x, y, 0, Color.red);
                            }
                            // Break out of the loop if overcharge is triggered, no more charge needed
                            break;
                        }
                    }
                }
            }
        }

        @Override
        public void draw() {
            super.draw(); // Draw the base drill sprites

            float barWidth = 0.8f;
            float barHeight = 0.12f;
            float barX = x - barWidth / 2f * Vars.tilesize;
            float barY = y + (size * Vars.tilesize / 2f) - (barHeight * Vars.tilesize * 1.5f); // Position above the block

            // Draw charge bar
            if (!inCooldown) {
                Draw.color(Color.darkGray);
                Fill.rect(barX, barY, barWidth * Vars.tilesize, barHeight * Vars.tilesize);

                if (isOvercharged) {
                    Draw.color(overchargeBarColor);
                    Fill.rect(barX, barY, (overchargeTimer / overchargeDuration) * barWidth * Vars.tilesize, barHeight * Vars.tilesize);
                } else {
                    Draw.color(chargeBarColor);
                    Fill.rect(barX, barY, (currentCharge / maxCharge) * barWidth * Vars.tilesize, barHeight * Vars.tilesize);
                }
            } else {
                // Draw cooldown bar
                Draw.color(Color.darkGray);
                Fill.rect(barX, barY, barWidth * Vars.tilesize, barHeight * Vars.tilesize);
                Draw.color(Color.orange); // Cooldown color
                Fill.rect(barX, barY, (cooldownTimer / cooldownDuration) * barWidth * Vars.tilesize, barHeight * Vars.tilesize);
            }

            Draw.color(); // Reset color
        }

        // --- Network Synchronization (Important for multiplayer) ---
        @Override
        public void write(arc.util.io.Writes write) {
            super.write(write);
            write.f(currentCharge);
            write.f(overchargeTimer);
            write.f(cooldownTimer);
            write.bool(isOvercharged);
            write.bool(inCooldown);
        }

        @Override
        public void read(arc.util.io.Reads read) {
            super.read(read);
            currentCharge = read.f();
            overchargeTimer = read.f();
            cooldownTimer = read.f();
            isOvercharged = read.bool();
            inCooldown = read.bool();

            // Adjust drillTime immediately on read if overcharged
            if(isOvercharged){
                drillTime = OverchargeDrill.this.drillTime * overchargeDrillTimeMultiplier;
            } else if (inCooldown) {
                // When reloading, if in cooldown, ensure drillTime is set back to normal
                drillTime = OverchargeDrill.this.drillTime;
            }
        }
    }
}
