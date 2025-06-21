package testdex.block; // This package declaration MUST match the directory: Javamodtest/src/testdex/block/

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
import mindustry.type.Category;

public class OverchargeDrill extends Drill {

    // --- Configurable Drill Properties ---
    public float maxCharge = 100f; // Max charge capacity for overcharge
    public float chargeChance = 0.5f; // Chance (0.0 to 1.0) to gain 1 charge per item produced
    public float overchargeDuration = 300f; // Duration of overcharge state in ticks
    public float overchargeDrillTimeMultiplier = 0.5f; // Multiplier for drillTime during overcharge (0.5 means 2x faster)
    public float cooldownDuration = 600f; // Duration of cooldown state in ticks
    public Effect overchargeEffect = Fx.hitLiquid; // Effect played when entering overcharge
    public Color chargeBarColor = Color.cyan; // Color for the charge bar
    public Color overchargeBarColor = Color.red; // Color for the overcharge bar

    public OverchargeDrill(String name) {
        super(name);
        update = true; // Essential: ensures updateTile() is called every tick
    }

    // --- Stats for Display in Info Panel (Optional but good practice) ---
    @Override
    public void setStats() {
        super.setStats(); // Include base drill stats

        // Add custom stats using Stat.name() for flexibility.
        // You'll need to define these string keys in your mod's bundle.properties file for proper display.
        stats.add(Stat.value, maxCharge, StatUnit.none);
        stats.add(Stat.value, (int)(chargeChance * 100) + "%", StatUnit.percent);
        stats.add(Stat.value, overchargeDuration / 60f, StatUnit.seconds);
        stats.add(Stat.value, cooldownDuration / 60f, StatUnit.seconds);
        
        // Display the effective drill speed during overcharge
        stats.add(Stat.drillSpeed, ((1f / (this.drillTime * overchargeDrillTimeMultiplier)) * 60f), StatUnit.itemsSecond);
    }

    // --- Custom Tile Entity for State Management and Logic ---
    public class OverchargeDrillBuild extends DrillBuild {
        public float currentCharge = 0f;
        public float overchargeTimer = 0f;
        public float cooldownTimer = 0f;
        public boolean isOvercharged = false;
        public boolean inCooldown = false;

        @Override
        public void updateTile() {
            // Logic for when the drill is in cooldown
            if (inCooldown) {
                cooldownTimer -= Vars.delta; // Decrement cooldown timer
                if (cooldownTimer <= 0) {
                    inCooldown = false; // Exit cooldown state
                    drillTime = OverchargeDrill.this.drillTime; // Reset drillTime to its original value
                    currentCharge = 0f; // Reset charge after cooldown period
                    Fx.reactorExplosion.at(x, y, 0f, Color.green); // Visual effect for exiting cooldown
                    if (Vars.net.client()) { // Play sound only on client side
                        Sounds.blockRevive.at(x, y);
                    }
                }
                return; // Stop further processing if in cooldown
            }

            // Logic for when the drill is in overcharge
            if (isOvercharged) {
                overchargeTimer -= Vars.delta; // Decrement overcharge timer
                if (overchargeTimer <= 0) {
                    // Overcharge ends, transition to cooldown
                    isOvercharged = false;
                    inCooldown = true;
                    cooldownTimer = cooldownDuration; // Set cooldown timer
                    Fx.vaporize.at(x, y, 0f, Color.red); // Visual effect for entering cooldown
                    if (Vars.net.client()) { // Play sound only on client side
                        Sounds.explosion.at(x, y);
                    }
                    return; // Stop further processing this tick, cooldown has begun
                }
                // During overcharge, the base drill's update will use the faster drillTime
                super.updateTile();
                return; // Stop further processing if overcharged
            }

            // --- Normal operation (not overcharged, not in cooldown) ---
            float oldProgress = this.progress; // Capture mining progress before calling super.updateTile()
            super.updateTile(); // Call the base Drill's updateTile for normal mining logic

            // Detect if an item was successfully mined by the base drill.
            // This is a heuristic: If mining progress was near completion (e.g., >0.99f) and
            // then reset to near zero (e.g., <0.01f), and the drill is warmed up, it implies a cycle completed.
            // Adjust the 0.99f and 0.01f thresholds if your specific drill's progress behavior varies.
            if (oldProgress > 0.99f && this.progress < 0.01f && this.warmup > 0.9f && !isOvercharged && !inCooldown) {
                // Check for a chance to gain 1 charge for this production cycle
                if (Mathf.random() < OverchargeDrill.this.chargeChance) {
                    currentCharge = Mathf.clamp(currentCharge + 1f, 0, maxCharge); // Gain exactly 1 charge, clamped

                    if (currentCharge >= maxCharge) {
                        // Full charge reached, activate overcharge!
                        isOvercharged = true;
                        overchargeTimer = overchargeDuration; // Start overcharge timer
                        currentCharge = maxCharge; // Ensure charge is exactly at max
                        
                        // Set drillTime to be faster during overcharge
                        drillTime = OverchargeDrill.this.drillTime * overchargeDrillTimeMultiplier;

                        // Visual and audio effects for overcharge activation
                        overchargeEffect.at(x, y);
                        if (Vars.net.client()) {
                            Sounds.laserbig.at(x, y);
                            Call.effect(Fx.reactorsmoke, x, y, 0, Color.red);
                        }
                    }
                }
            }
        }

        @Override
        public void draw() {
            super.draw(); // Draw the base drill sprites

            // Calculate position and size for the custom charge/cooldown bar
            float barWidth = 0.8f;
            float barHeight = 0.12f;
            float barX = x - barWidth / 2f * Vars.tilesize;
            float barY = y + (size * Vars.tilesize / 2f) - (barHeight * Vars.tilesize * 1.5f); // Position above the block

            // Draw the charge bar if not in cooldown
            if (!inCooldown) {
                Draw.color(Color.darkGray); // Background of the bar
                Fill.rect(barX, barY, barWidth * Vars.tilesize, barHeight * Vars.tilesize);

                if (isOvercharged) {
                    // Draw overcharge progress
                    Draw.color(overchargeBarColor);
                    Fill.rect(barX, barY, (overchargeTimer / overchargeDuration) * barWidth * Vars.tilesize, barHeight * Vars.tilesize);
                } else {
                    // Draw normal charge progress
                    Draw.color(chargeBarColor);
                    Fill.rect(barX, barY, (currentCharge / maxCharge) * barWidth * Vars.tilesize, barHeight * Vars.tilesize);
                }
            } else {
                // Draw cooldown bar if in cooldown
                Draw.color(Color.darkGray); // Background of the bar
                Fill.rect(barX, barY, barWidth * Vars.tilesize, barHeight * Vars.tilesize);
                Draw.color(Color.orange); // Cooldown color
                Fill.rect(barX, barY, (cooldownTimer / cooldownDuration) * barWidth * Vars.tilesize, barHeight * Vars.tilesize);
            }

            Draw.color(); // Reset drawing color to default
        }

        // --- Network Synchronization (Important for multiplayer and saving/loading) ---
        @Override
        public void write(arc.util.io.Writes write) {
            super.write(write); // Write base DrillBuild data
            // Write custom state variables
            write.f(currentCharge);
            write.f(overchargeTimer);
            write.f(cooldownTimer);
            write.bool(isOvercharged);
            write.bool(inCooldown);
        }

        @Override
        public void read(arc.util.io.Reads read) {
            super.read(read); // Read base DrillBuild data
            // Read custom state variables
            currentCharge = read.f();
            overchargeTimer = read.f();
            cooldownTimer = read.f();
            isOvercharged = read.bool();
            inCooldown = read.bool();

            // Adjust drillTime immediately on read based on loaded state
            if(isOvercharged){
                drillTime = OverchargeDrill.this.drillTime * overchargeDrillTimeMultiplier;
            } else if (inCooldown) {
                // When reloading, if in cooldown, ensure drillTime is set back to normal
                drillTime = OverchargeDrill.this.drillTime;
            }
        }
    }
}
