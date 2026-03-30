# HorseHusbandry

Adds titles detailing horse stats on mount.

Changes horse stat inheritance:  
Vanilla result is randomized in an evenly-distributed range around the parents' averages.
In addition, if a child exceeds the maximum stats it could receive, the excess is subtracted from the
maximum possible value. This means that as you close on a horse with "perfect" stats, the odds of receiving
better stats get weighted against you.  
This plugin weights the odds of the randomized addition to the parents' average stats towards increasing,
and any value exceeding the maximums for a stat is capped to that maximum rather than decreasing from it.

For example, one might breed two horses with an average speed of `0.32`. The maximum speed a horse
may have is `0.3375`. If the child rolled an increase of `0.05`, the vanilla child would have a speed of `0.305`
(`0.32 average + 0.05 random modifier = 0.37`. `0.37 > 0.3375`, so the excess is subtracted.
The final stat is `0.3375 - (0.37 - 0.3375) = 0.305`). With this plugin, the child will have a speed of `0.3375`.
