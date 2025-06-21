I can certainly adjust the ChargeDrill logic to only gain 1 charge per item, with a chance to gain it instead of a fixed amount.

Here's how we'll modify the ChargeDrill class:

Remove chargeGainPerItem: Since it's always 1 charge per successful gain, this variable is no longer needed in its previous form.
Add chargeChance: A new float variable to define the probability (0.0 to 1.0) of gaining 1 charge.
Modify mineItem(): Implement the random chance logic before incrementing the currentCharge.
Here's the updated ChargeDrill class:

Java

package your.mod.content; // Adjust package as needed

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
import mindustry.world.meta.Stat;
import mindustry.world.meta.StatUnit;

public class ChargeDrill extends Drill {

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

    public ChargeDrill(String name) {
        super(name);
        update = true; // Essential: allows updateTile() to be called
        has	
    }

    // --- Stats for Display (Optional but good practice) ---
    @Override
    public void set	
    }

    // --- Custom Tile Entity for State Management ---
    public class ChargeDrillBuild extends DrillBuild {
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
                    drillTime = ChargeDrill.this.drillTime; 
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
                    if (Mathf.random() < ChargeDrill.this.chargeChance) { // Check the chance for each item
                        currentCharge = Mathf.clamp(currentCharge + 1f, 0, maxCharge); // Gain exactly 1 charge

                        if (currentCharge >= maxCharge) {
                            // Full charge, activate overcharge!
                            isOvercharged = true;
                            overchargeTimer = overchargeDuration;
                            currentCharge = maxCharge; // Ensure it stays at max
                            
                            // Set drillTime to be faster during overcharge
                            drillTime = ChargeDrill.this.drillTime * overchargeDrillTimeMultiplier;

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
                drillTime = ChargeDrill.this.drillTime * overchargeDrillTimeMultiplier;
            } else if (inCooldown) {
                // When reloading, if in cooldown, ensure drillTime is set back to normal
                drillTime = ChargeDrill.this.drillTime; 
            }
        }
    }
}
@Override
protected void mineItem(Block block, int amount) {
    super.mineItem(block, amount); // Let the base drill do its job first

    // Gain charge for each item produced with a chance (if not already full or in cooldown)
    if (!isOvercharged && !inCooldown) {
        // Loop 'amount' times, as 'amount' items might have been produced in one go
        for (int i = 0; i < amount; i++) {
            if (Mathf.random() < ChargeDrill.this.chargeChance) { // Check the chance for each item
                currentCharge = Mathf.clamp(currentCharge + 1f, 0, maxCharge); // Gain exactly 1 charge

                if (currentCharge >= maxCharge) {
                    // Full charge, activate overcharge!
                    // ... (rest of the overcharge activation logic) ...
                    break; // Important: Stop trying to gain more charge if overcharge is triggered
                }
            }
        }
    }
}
