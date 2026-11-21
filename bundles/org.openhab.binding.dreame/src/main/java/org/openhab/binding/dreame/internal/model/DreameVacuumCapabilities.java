/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.openhab.binding.dreame.internal.model;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Vacuum models with protocol mappings in the Home Assistant Dreame integration.
 *
 * <p>
 * The property and action addresses used by this binding are the common Dreame mappings. Optional capabilities are
 * detected from successful property responses, so unavailable model-specific features remain disabled.
 *
 * @author Ronny Grun - Initial contribution
 */
@NonNullByDefault
public final class DreameVacuumCapabilities {
    // Model identifiers listed by the Home Assistant Dreame Vacuum integration. Legacy identifiers are retained.
    private static final Set<String> SUPPORTED_MODELS = Set.copyOf(Arrays.asList("""
            dreame.vacuum.c102cn
            dreame.vacuum.c102gl
            dreame.vacuum.d103cn
            dreame.vacuum.d110ch
            dreame.vacuum.p2008
            dreame.vacuum.p2009
            dreame.vacuum.p2027
            dreame.vacuum.p2028
            dreame.vacuum.p2028a
            dreame.vacuum.p2029
            dreame.vacuum.p2156o
            dreame.vacuum.p2157
            dreame.vacuum.p2187
            dreame.vacuum.p2259
            dreame.vacuum.r2104
            dreame.vacuum.r2205
            dreame.vacuum.r2212
            dreame.vacuum.r2215
            dreame.vacuum.r2216o
            dreame.vacuum.r2228
            dreame.vacuum.r2228d
            dreame.vacuum.r2228o
            dreame.vacuum.r2228z
            dreame.vacuum.r2232a
            dreame.vacuum.r2232b
            dreame.vacuum.r2232c
            dreame.vacuum.r2232d
            dreame.vacuum.r2233
            dreame.vacuum.r2235
            dreame.vacuum.r2235a
            dreame.vacuum.r2240
            dreame.vacuum.r2243
            dreame.vacuum.r2246
            dreame.vacuum.r2247
            dreame.vacuum.r2250
            dreame.vacuum.r2251a
            dreame.vacuum.r2251o
            dreame.vacuum.r2253
            dreame.vacuum.r2253b
            dreame.vacuum.r2253c
            dreame.vacuum.r2253d
            dreame.vacuum.r2253m
            dreame.vacuum.r2253t
            dreame.vacuum.r2253u
            dreame.vacuum.r2253w
            dreame.vacuum.r2257o
            dreame.vacuum.r2260
            dreame.vacuum.r2263
            dreame.vacuum.r2273
            dreame.vacuum.r2273a
            dreame.vacuum.r2310
            dreame.vacuum.r2310a
            dreame.vacuum.r2310b
            dreame.vacuum.r2310d
            dreame.vacuum.r2310e
            dreame.vacuum.r2310f
            dreame.vacuum.r2310g
            dreame.vacuum.r2312
            dreame.vacuum.r2312a
            dreame.vacuum.r2313
            dreame.vacuum.r2316
            dreame.vacuum.r2316p
            dreame.vacuum.r2317
            dreame.vacuum.r2322
            dreame.vacuum.r2328
            dreame.vacuum.r2332
            dreame.vacuum.r2338
            dreame.vacuum.r2338a
            dreame.vacuum.r2338h
            dreame.vacuum.r2345a
            dreame.vacuum.r2345h
            dreame.vacuum.r2350
            dreame.vacuum.r2355
            dreame.vacuum.r2360
            dreame.vacuum.r2360w
            dreame.vacuum.r2361a
            dreame.vacuum.r2363
            dreame.vacuum.r2363a
            dreame.vacuum.r2363n
            dreame.vacuum.r2364
            dreame.vacuum.r23646
            dreame.vacuum.r2364a
            dreame.vacuum.r2367
            dreame.vacuum.r2375
            dreame.vacuum.r2377
            dreame.vacuum.r2380
            dreame.vacuum.r2382a
            dreame.vacuum.r2382k
            dreame.vacuum.r2383a
            dreame.vacuum.r2383k
            dreame.vacuum.r2385
            dreame.vacuum.r2385a
            dreame.vacuum.r2386
            dreame.vacuum.r2388
            dreame.vacuum.r2394a
            dreame.vacuum.r2394f
            dreame.vacuum.r2394j
            dreame.vacuum.r2394k
            dreame.vacuum.r2394l
            dreame.vacuum.r2394s
            dreame.vacuum.r2394u
            dreame.vacuum.r2398
            dreame.vacuum.r2401
            dreame.vacuum.r2401a
            dreame.vacuum.r2401h
            dreame.vacuum.r2401u
            dreame.vacuum.r2412
            dreame.vacuum.r2416
            dreame.vacuum.r24162
            dreame.vacuum.r2416a
            dreame.vacuum.r2416c
            dreame.vacuum.r2416f
            dreame.vacuum.r2416h
            dreame.vacuum.r2416n
            dreame.vacuum.r2421
            dreame.vacuum.r2422
            dreame.vacuum.r2422a
            dreame.vacuum.r2422b
            dreame.vacuum.r2422c
            dreame.vacuum.r2423
            dreame.vacuum.r2423a
            dreame.vacuum.r2424
            dreame.vacuum.r2426
            dreame.vacuum.r2427
            dreame.vacuum.r2427a
            dreame.vacuum.r2430a
            dreame.vacuum.r2430u
            dreame.vacuum.r2432
            dreame.vacuum.r2432b
            dreame.vacuum.r2435
            dreame.vacuum.r2437
            dreame.vacuum.r2438a
            dreame.vacuum.r2438b
            dreame.vacuum.r2438i
            dreame.vacuum.r2440
            dreame.vacuum.r2441
            dreame.vacuum.r2443
            dreame.vacuum.r24495
            dreame.vacuum.r2449a
            dreame.vacuum.r2449b
            dreame.vacuum.r2449k
            dreame.vacuum.r2450m
            dreame.vacuum.r2455
            dreame.vacuum.r2458a
            dreame.vacuum.r2458h
            dreame.vacuum.r2459a
            dreame.vacuum.r2459h
            dreame.vacuum.r2459k
            dreame.vacuum.r2462
            dreame.vacuum.r2462a
            dreame.vacuum.r2465a
            dreame.vacuum.r2465b
            dreame.vacuum.r2465h
            dreame.vacuum.r2467
            dreame.vacuum.r2469a
            dreame.vacuum.r2469x
            dreame.vacuum.r2471
            dreame.vacuum.r2473
            dreame.vacuum.r2473s
            dreame.vacuum.r2475a
            dreame.vacuum.r2475h
            dreame.vacuum.r2475t
            dreame.vacuum.r2483
            dreame.vacuum.r2485
            dreame.vacuum.r2487
            dreame.vacuum.r2488
            dreame.vacuum.r2489
            dreame.vacuum.r24896
            dreame.vacuum.r24898
            dreame.vacuum.r2489a
            dreame.vacuum.r2489b
            dreame.vacuum.r2489c
            dreame.vacuum.r2489d
            dreame.vacuum.r2489e
            dreame.vacuum.r2489f
            dreame.vacuum.r2489h
            dreame.vacuum.r2489j
            dreame.vacuum.r2489k
            dreame.vacuum.r2489l
            dreame.vacuum.r2489p
            dreame.vacuum.r2489q
            dreame.vacuum.r2489s
            dreame.vacuum.r2489t
            dreame.vacuum.r2489u
            dreame.vacuum.r2489z
            dreame.vacuum.r2491
            dreame.vacuum.r2491a
            dreame.vacuum.r2492a
            dreame.vacuum.r2492b
            dreame.vacuum.r2492j
            dreame.vacuum.r2493
            dreame.vacuum.r2495
            dreame.vacuum.r2501
            dreame.vacuum.r2501l
            dreame.vacuum.r2501p
            dreame.vacuum.r2502
            dreame.vacuum.r2504
            dreame.vacuum.r2505
            dreame.vacuum.r2505z
            dreame.vacuum.r2507
            dreame.vacuum.r2508
            dreame.vacuum.r2513
            dreame.vacuum.r25135
            dreame.vacuum.r2513a
            dreame.vacuum.r2513f
            dreame.vacuum.r2513h
            dreame.vacuum.r2513k
            dreame.vacuum.r2513p
            dreame.vacuum.r2513u
            dreame.vacuum.r2515
            dreame.vacuum.r2518
            dreame.vacuum.r2518a
            dreame.vacuum.r2518f
            dreame.vacuum.r2518j
            dreame.vacuum.r2518p
            dreame.vacuum.r2519a
            dreame.vacuum.r2520
            dreame.vacuum.r2525a
            dreame.vacuum.r2525e
            dreame.vacuum.r2525h
            dreame.vacuum.r2527
            dreame.vacuum.r2527a
            dreame.vacuum.r2527b
            dreame.vacuum.r2527g
            dreame.vacuum.r2527h
            dreame.vacuum.r2527j
            dreame.vacuum.r2527q
            dreame.vacuum.r2527t
            dreame.vacuum.r2527u
            dreame.vacuum.r2531
            dreame.vacuum.r2532a
            dreame.vacuum.r2532d
            dreame.vacuum.r2532h
            dreame.vacuum.r2532v
            dreame.vacuum.r2532w
            dreame.vacuum.r2532z
            dreame.vacuum.r2538a
            dreame.vacuum.r2538z
            dreame.vacuum.r2548
            dreame.vacuum.r25513
            dreame.vacuum.r2551a
            dreame.vacuum.r2551h
            dreame.vacuum.r2553
            dreame.vacuum.r2560
            dreame.vacuum.r2562a
            dreame.vacuum.r2562b
            dreame.vacuum.r2562c
            dreame.vacuum.r2562h
            dreame.vacuum.r2562k
            dreame.vacuum.r2562s
            dreame.vacuum.r2562t
            dreame.vacuum.r2562u
            dreame.vacuum.r2563b
            dreame.vacuum.r2563v
            dreame.vacuum.r25642
            dreame.vacuum.r2564b
            dreame.vacuum.r2564s
            dreame.vacuum.r2564z
            dreame.vacuum.r2565a
            dreame.vacuum.r2565v
            dreame.vacuum.r2566a
            dreame.vacuum.r2566h
            dreame.vacuum.r2567a
            dreame.vacuum.r2569c
            dreame.vacuum.r2571
            dreame.vacuum.r2572
            dreame.vacuum.r2573
            dreame.vacuum.r2574
            dreame.vacuum.r2575
            dreame.vacuum.r2576
            dreame.vacuum.r25799
            dreame.vacuum.r2579a
            dreame.vacuum.r2579c
            dreame.vacuum.r2579h
            dreame.vacuum.r2579k
            dreame.vacuum.r2580
            dreame.vacuum.r2581
            dreame.vacuum.r2582a
            dreame.vacuum.r2582c
            dreame.vacuum.r2582h
            dreame.vacuum.r2582k
            dreame.vacuum.r2584g
            dreame.vacuum.r25856
            dreame.vacuum.r25857
            dreame.vacuum.r25858
            dreame.vacuum.r25859
            dreame.vacuum.r500a
            dreame.vacuum.r500b
            dreame.vacuum.r500b1
            dreame.vacuum.r500c
            dreame.vacuum.r500c1
            dreame.vacuum.r500da
            dreame.vacuum.r500n
            dreame.vacuum.r500vh
            dreame.vacuum.r500vp
            dreame.vacuum.r500xb
            dreame.vacuum.r500yb
            dreame.vacuum.r500yc
            dreame.vacuum.r500ye
            dreame.vacuum.r500yt
            dreame.vacuum.r500za
            dreame.vacuum.r500zc
            dreame.vacuum.r500zh
            dreame.vacuum.r500zk
            dreame.vacuum.r5016
            dreame.vacuum.r501ba
            dreame.vacuum.r501bc
            dreame.vacuum.r501bk
            dreame.vacuum.r501bt
            dreame.vacuum.r501da
            dreame.vacuum.r501h
            dreame.vacuum.r501he
            dreame.vacuum.r501s
            dreame.vacuum.r501st
            dreame.vacuum.r501t
            dreame.vacuum.r501tt
            dreame.vacuum.r501ua
            dreame.vacuum.r501vr
            dreame.vacuum.r501vt
            dreame.vacuum.r501w
            dreame.vacuum.r501wu
            dreame.vacuum.r5020a
            dreame.vacuum.r5021a
            dreame.vacuum.r5021b
            dreame.vacuum.r5021c
            dreame.vacuum.r5021h
            dreame.vacuum.r5021k
            dreame.vacuum.r5021s
            dreame.vacuum.r5021t
            dreame.vacuum.r5021u
            dreame.vacuum.r5022a
            dreame.vacuum.r5023a
            dreame.vacuum.r5023e
            dreame.vacuum.r5024b
            dreame.vacuum.r5024t
            dreame.vacuum.r5025b
            dreame.vacuum.r5025t
            dreame.vacuum.r502ca
            dreame.vacuum.r502k3
            dreame.vacuum.r502ka
            dreame.vacuum.r502ke
            dreame.vacuum.r502la
            dreame.vacuum.r502le
            dreame.vacuum.r502lh
            dreame.vacuum.r502mf
            dreame.vacuum.r502mn
            dreame.vacuum.r502mt
            dreame.vacuum.r502va
            dreame.vacuum.r5039
            dreame.vacuum.r50391
            dreame.vacuum.r50393
            dreame.vacuum.r50395
            dreame.vacuum.r50397
            dreame.vacuum.r50398
            dreame.vacuum.r50399
            dreame.vacuum.r5039a
            dreame.vacuum.r5039e
            dreame.vacuum.r5039f
            dreame.vacuum.r5039h
            dreame.vacuum.r5039r
            dreame.vacuum.r5039w
            dreame.vacuum.r50485
            dreame.vacuum.r5048a
            dreame.vacuum.r5048h
            dreame.vacuum.r5049
            dreame.vacuum.r50573
            dreame.vacuum.r5057a
            dreame.vacuum.r5057k
            dreame.vacuum.r5057r
            dreame.vacuum.r5061
            dreame.vacuum.r5061a
            dreame.vacuum.r5061e
            dreame.vacuum.r50625
            dreame.vacuum.r5062a
            dreame.vacuum.r5062h
            dreame.vacuum.r5062k
            dreame.vacuum.r5064t
            dreame.vacuum.r5064u
            dreame.vacuum.r5082a
            dreame.vacuum.r5082c
            dreame.vacuum.r5085
            dreame.vacuum.r50855
            dreame.vacuum.r5089b
            dreame.vacuum.r5089u
            dreame.vacuum.r5090a
            dreame.vacuum.r5099
            dreame.vacuum.r5099a
            dreame.vacuum.r5099e
            dreame.vacuum.r5099g
            dreame.vacuum.r5099h
            dreame.vacuum.r5099k
            dreame.vacuum.r5104h
            dreame.vacuum.r5109f
            dreame.vacuum.r510a
            dreame.vacuum.r510b
            dreame.vacuum.r510b1
            dreame.vacuum.r510c
            dreame.vacuum.r510c1
            dreame.vacuum.r510n
            dreame.vacuum.r5117
            dreame.vacuum.r512g
            dreame.vacuum.r5139
            dreame.vacuum.r51395
            dreame.vacuum.r5149
            dreame.vacuum.r5185
            dreame.vacuum.r51855
            dreame.vacuum.r5189j
            dreame.vacuum.r5189u
            dreame.vacuum.r520b
            dreame.vacuum.r520b1
            dreame.vacuum.r520c
            dreame.vacuum.r520c1
            dreame.vacuum.r520n
            dreame.vacuum.r522kf
            dreame.vacuum.r5239f
            dreame.vacuum.r5299
            dreame.vacuum.r530jb
            dreame.vacuum.r530ua
            dreame.vacuum.r53148
            dreame.vacuum.r5314a
            dreame.vacuum.r5314s
            dreame.vacuum.r5314z
            dreame.vacuum.r531ra
            dreame.vacuum.r531rh
            dreame.vacuum.r53456
            dreame.vacuum.r5345b
            dreame.vacuum.r5345e
            dreame.vacuum.r540ga
            dreame.vacuum.r540jb
            dreame.vacuum.r540tb
            dreame.vacuum.r54125
            dreame.vacuum.r5412a
            dreame.vacuum.r5412v
            dreame.vacuum.r54136
            dreame.vacuum.r5413a
            dreame.vacuum.r5413v
            dreame.vacuum.r542q
            dreame.vacuum.r6001a
            dreame.vacuum.r6011
            dreame.vacuum.r6012
            dreame.vacuum.r6015a
            dreame.vacuum.r6016
            dreame.vacuum.r6017b
            dreame.vacuum.r6017q
            dreame.vacuum.r6018a
            dreame.vacuum.r6018c
            dreame.vacuum.r6047a
            dreame.vacuum.r60u2k
            dreame.vacuum.r6101h
            dreame.vacuum.r6111
            dreame.vacuum.r6112
            dreame.vacuum.r6116
            dreame.vacuum.r6117j
            dreame.vacuum.r6216
            dreame.vacuum.r63013
            dreame.vacuum.r63014
            dreame.vacuum.r63015
            dreame.vacuum.r63017
            dreame.vacuum.r6301a
            dreame.vacuum.r6301h
            dreame.vacuum.r6301r
            dreame.vacuum.r6301v
            dreame.vacuum.r64015
            dreame.vacuum.r64017
            dreame.vacuum.r64018
            dreame.vacuum.r6401c
            dreame.vacuum.r6401k
            dreame.vacuum.r6401r
            dreame.vacuum.r6401v
            dreame.vacuum.r9301
            dreame.vacuum.r9302
            dreame.vacuum.r9304
            dreame.vacuum.r9305
            dreame.vacuum.r9309a
            dreame.vacuum.r9311
            dreame.vacuum.r9312
            dreame.vacuum.r9316a
            dreame.vacuum.r9316h
            dreame.vacuum.r9316k
            dreame.vacuum.r9317a
            dreame.vacuum.r9401
            dreame.vacuum.r9406
            dreame.vacuum.r9408
            dreame.vacuum.r9416d
            dreame.vacuum.r9419
            dreame.vacuum.r9419a
            dreame.vacuum.r9419e
            dreame.vacuum.r9419h
            dreame.vacuum.r9419t
            dreame.vacuum.r9420
            dreame.vacuum.r9421
            dreame.vacuum.r9421t
            dreame.vacuum.r9422
            dreame.vacuum.r9425
            dreame.vacuum.r9426
            dreame.vacuum.r9426x
            dreame.vacuum.r9427h
            dreame.vacuum.r9428
            dreame.vacuum.r9430
            dreame.vacuum.r9434
            dreame.vacuum.r94345
            dreame.vacuum.r94346
            dreame.vacuum.r9434h
            dreame.vacuum.r9434j
            dreame.vacuum.r9434l
            dreame.vacuum.r9434v
            dreame.vacuum.r9441
            dreame.vacuum.r9445a
            dreame.vacuum.r9445d
            dreame.vacuum.r9445g
            dreame.vacuum.r9446a
            dreame.vacuum.r9446b
            dreame.vacuum.r9446c
            dreame.vacuum.r9446h
            dreame.vacuum.r9446j
            dreame.vacuum.r9446k
            dreame.vacuum.r9455
            dreame.vacuum.r9455z
            dreame.vacuum.r9461
            dreame.vacuum.r9471
            dreame.vacuum.r9471z
            dreame.vacuum.r9476
            dreame.vacuum.r9477
            dreame.vacuum.r9478
            dreame.vacuum.r9479
            dreame.vacuum.r9483
            dreame.vacuum.r9486
            dreame.vacuum.r9486z
            dreame.vacuum.r9487
            dreame.vacuum.r9488
            dreame.vacuum.r9489
            dreame.vacuum.r9490
            dreame.vacuum.r9491
            dreame.vacuum.r9493a
            dreame.vacuum.r9493c
            dreame.vacuum.r9493h
            dreame.vacuum.r9498
            dreame.vacuum.r9504a
            dreame.vacuum.r9506
            dreame.vacuum.r9507b
            dreame.vacuum.r9508b
            dreame.vacuum.r9513
            dreame.vacuum.r9515a
            dreame.vacuum.r9515e
            dreame.vacuum.r9516
            dreame.vacuum.r9517
            dreame.vacuum.r9518
            dreame.vacuum.r9519
            dreame.vacuum.r95239
            dreame.vacuum.r9523a
            dreame.vacuum.r9523b
            dreame.vacuum.r9523c
            dreame.vacuum.r9523h
            dreame.vacuum.r9523k
            dreame.vacuum.r9523t
            dreame.vacuum.r9523u
            dreame.vacuum.r95249
            dreame.vacuum.r9524a
            dreame.vacuum.r9524b
            dreame.vacuum.r9524c
            dreame.vacuum.r9524h
            dreame.vacuum.r9524j
            dreame.vacuum.r9524k
            dreame.vacuum.r9524m
            dreame.vacuum.r95265
            dreame.vacuum.r95266
            dreame.vacuum.r95267
            dreame.vacuum.r9526a
            dreame.vacuum.r9526c
            dreame.vacuum.r9526h
            dreame.vacuum.r9526k
            dreame.vacuum.r9527
            dreame.vacuum.r95275
            dreame.vacuum.r95276
            dreame.vacuum.r95277
            dreame.vacuum.r95278
            dreame.vacuum.r95279
            dreame.vacuum.r9527a
            dreame.vacuum.r9527f
            dreame.vacuum.r9527v
            dreame.vacuum.r95285
            dreame.vacuum.r9528a
            dreame.vacuum.r9528h
            dreame.vacuum.r9533a
            dreame.vacuum.r9533h
            dreame.vacuum.r9533t
            dreame.vacuum.r9535a
            dreame.vacuum.r9535h
            dreame.vacuum.r9535n
            dreame.vacuum.r9537a
            dreame.vacuum.r9537h
            dreame.vacuum.r95385
            dreame.vacuum.r9538a
            dreame.vacuum.r9538b
            dreame.vacuum.r9538c
            dreame.vacuum.r9538e
            dreame.vacuum.r9538h
            dreame.vacuum.r9538k
            dreame.vacuum.r9538l
            dreame.vacuum.r9538t
            dreame.vacuum.r9538u
            dreame.vacuum.r9540n
            dreame.vacuum.r95425
            dreame.vacuum.r9542a
            dreame.vacuum.r9542b
            dreame.vacuum.r9542h
            dreame.vacuum.r9542t
            dreame.vacuum.r95475
            dreame.vacuum.r95476
            dreame.vacuum.r9547a
            dreame.vacuum.r9547c
            dreame.vacuum.r9547h
            dreame.vacuum.r9547k
            dreame.vacuum.r9548
            dreame.vacuum.r9551
            mova.vacuum.r2430
            mova.vacuum.r2430a
            mova.vacuum.r2430u
            mova.vacuum.r2432b
            mova.vacuum.r2462a
            mova.vacuum.r2475a
            mova.vacuum.r2475h
            mova.vacuum.r2475t
            mova.vacuum.r24916
            mova.vacuum.r2491a
            mova.vacuum.r2519a
            mova.vacuum.r2525a
            mova.vacuum.r2525e
            mova.vacuum.r2525h
            mova.vacuum.r2533h
            mova.vacuum.r2534h
            mova.vacuum.r2535
            mova.vacuum.r2561
            mova.vacuum.r2567a
            mova.vacuum.r2569c
            mova.vacuum.r2570
            mova.vacuum.r2582a
            mova.vacuum.r2582c
            mova.vacuum.r2582h
            mova.vacuum.r2582k
            mova.vacuum.r2587a
            mova.vacuum.r2599
            mova.vacuum.r2599h
            mova.vacuum.r2599k
            mova.vacuum.r2599u
            mova.vacuum.r5730
            mova.vacuum.r5730c
            mova.vacuum.r5732a
            mova.vacuum.r5747
            mova.vacuum.r5765h
            mova.vacuum.r5765k
            mova.vacuum.r5765p
            mova.vacuum.r5765q
            mova.vacuum.r5765u
            mova.vacuum.r5766
            mova.vacuum.r5766h
            mova.vacuum.r5766k
            mova.vacuum.r5766q
            mova.vacuum.r5769
            mova.vacuum.r57695
            mova.vacuum.r57698
            mova.vacuum.r5769a
            mova.vacuum.r5769f
            mova.vacuum.r5769g
            mova.vacuum.r5769h
            mova.vacuum.r5769q
            mova.vacuum.r5769t
            mova.vacuum.r5770
            mova.vacuum.r5770a
            mova.vacuum.r5770g
            mova.vacuum.r5770h
            mova.vacuum.r5770q
            mova.vacuum.r5770t
            mova.vacuum.r5871
            mova.vacuum.r58715
            mova.vacuum.r590q
            mova.vacuum.r590qf
            mova.vacuum.r5977a
            mova.vacuum.r5977c
            mova.vacuum.r5977f
            mova.vacuum.r5977g
            mova.vacuum.r5977h
            mova.vacuum.r5977k
            mova.vacuum.r5977t
            mova.vacuum.r5978a
            mova.vacuum.r5978h
            mova.vacuum.r5978q
            mova.vacuum.r59795
            mova.vacuum.r59796
            mova.vacuum.r59797
            mova.vacuum.r59799
            mova.vacuum.r5979a
            mova.vacuum.r5979f
            mova.vacuum.r5979g
            mova.vacuum.r5979h
            mova.vacuum.r5979t
            mova.vacuum.r5979u
            mova.vacuum.r67095
            mova.vacuum.r6709a
            mova.vacuum.r6709c
            mova.vacuum.r6709g
            mova.vacuum.r6709h
            mova.vacuum.r6710a
            mova.vacuum.r6710g
            mova.vacuum.r6710h
            mova.vacuum.r6710t
            mova.vacuum.r9416
            mova.vacuum.r94165
            mova.vacuum.r9416d
            mova.vacuum.r9427
            mova.vacuum.r9427h
            mova.vacuum.r9427x
            mova.vacuum.r9473
            mova.vacuum.r9473e
            mova.vacuum.r9474
            mova.vacuum.r94745
            mova.vacuum.r9480
            mova.vacuum.r9482
            mova.vacuum.r9503
            mova.vacuum.r95046
            mova.vacuum.r95049
            mova.vacuum.r9504a
            mova.vacuum.r95406
            mova.vacuum.r9540a
            mova.vacuum.r9540h
            mova.vacuum.r9540k
            mova.vacuum.r9540n
            mova.vacuum.r9540u
            mova.vacuum.r9543
            mova.vacuum.r9543a
            mova.vacuum.r9543b
            mova.vacuum.r95445
            mova.vacuum.r9544a
            mova.vacuum.r9544b
            mova.vacuum.r9544t
            mova.vacuum.r9549
            """.strip().split("\\R")));

    // State-schema capabilities synchronized with Tasshack/dreame-vacuum DEVICE_INFO at commit
    // 8556ef85132c3d73a5288dd00738636392dac633.
    private static final Set<String> OLD_STATE_MODELS = prefixedModels("""
            p2008 p2009 p2027 p2028 p2028a p2029 p2036 p2041 p2041o p2114a p2114o p2140 p2140a p2140o
            p2140p p2140q p2148o p2149o p2150a p2150b p2150o p2156o p2157 p2187 p2259 r2104 r2205 r2209
            r2210 r2211o r2216o r2228 r2228d r2228o r2228z r2232a r2232b r2232c r2232d r2233 r2240 r2243
            r2246 r2247 r2250 r2251a r2251o r2254 r2257o r2260 r2312 r2312a r2322 r2328 r2380 r2380r r2388
            r2458a r2458h r2459a r2459h r2459k r2459r r2463r r2471 r2478r r2478v r2567a r2567r r2569c
            r2569r r2570
            """);

    // Models without the MOP_PAD_LIFTING capability in Tasshack/dreame-vacuum DEVICE_INFO at commit
    // 8556ef85132c3d73a5288dd00738636392dac633. Models with a self-wash base are detected at runtime through 4/25.
    private static final Set<String> NON_LIFTING_MODELS = prefixedModels("""
            p2008 p2009 p2027 p2028 p2028a p2029 p2036 p2041 p2041o p2140 p2140a p2140o p2140p p2140q
            p2148o p2150a p2150b p2150o p2156o p2157 p2187 p2259 r2104 r2205 r2209 r2211o r2240 r2243 r2250
            r2254 r2260 r2312 r2312a r2322 r2328 r2380 r2380r r2388 r2422 r2422a r2422b r2423 r2423a r2438a
            r2438b r2438i r2438r r2458a r2458h r2459a r2459h r2459k r2563b r2563v r2564b r2564s r2564z r2565a
            r2565v r2566a r2566h r2567a r2569c r9523a r9523b r9523c r9523h r9523k r9524a r9524b r9524c r9524h
            r9524k
            """);

    private static final Map<String, Integer> NEW_STATE_MIN_FIRMWARE = Map.ofEntries(
            Map.entry("dreame.vacuum.r2253", 1051), Map.entry("dreame.vacuum.r2263", 1051),
            Map.entry("dreame.vacuum.r2273", 1051), Map.entry("dreame.vacuum.r2273a", 1051),
            Map.entry("dreame.vacuum.r2313", 1019), Map.entry("dreame.vacuum.r2316", 1019),
            Map.entry("dreame.vacuum.r2316p", 1019), Map.entry("dreame.vacuum.r2317", 1114),
            Map.entry("dreame.vacuum.r2332", 5), Map.entry("dreame.vacuum.r2355", 1019),
            Map.entry("dreame.vacuum.r2360", 1019), Map.entry("dreame.vacuum.r2360w", 1019),
            Map.entry("dreame.vacuum.r2367", 5), Map.entry("dreame.vacuum.r2386", 1114),
            Map.entry("dreame.vacuum.r2398", 1019), Map.entry("dreame.vacuum.r2421", 1019));

    private DreameVacuumCapabilities() {
    }

    public static boolean isSupported(DreameDevice device) {
        return isSupported(device.model());
    }

    public static boolean isSupported(String model) {
        return SUPPORTED_MODELS.contains(model);
    }

    public static boolean usesNewStateSchema(DreameDevice device) {
        String model = device.model();
        if (OLD_STATE_MODELS.contains(model)) {
            return false;
        }
        Integer minimum = NEW_STATE_MIN_FIRMWARE.get(model);
        return minimum == null || firmwareBuild(device.version()) >= minimum;
    }

    public static boolean usesReversedCleaningMode(DreameDevice device, boolean selfWashBase) {
        return selfWashBase || !NON_LIFTING_MODELS.contains(device.model());
    }

    private static Set<String> prefixedModels(String models) {
        Set<String> result = new HashSet<>();
        for (String model : models.strip().split("\\s+")) {
            result.add("dreame.vacuum." + model);
        }
        return Set.copyOf(result);
    }

    private static int firmwareBuild(String version) {
        int separator = version.lastIndexOf('_');
        String build = separator >= 0 ? version.substring(separator + 1) : version;
        try {
            return Integer.parseInt(build);
        } catch (NumberFormatException e) {
            return 1;
        }
    }
}
