#Spotify Streamer Implementation Guide
[Click here for the full guide](https://docs.google.com/document/d/18GcAc70wUlllHUDoAAVF36fX9YSkuMyX07OWVQ2tDWI/pub?embedded=true#h.p69hpyxp4b00)

# <a id="h.vvxvgd8zz1vj" name="h.vvxvgd8zz1vj"></a><span>Stage 1: Overview</span>

<span></span>

<span>This document should be used as a guide and development plan for Stage 1 of the Spotify Streamer app. It breaks down the build process into smaller, concrete milestones, and provides technical guidance on specific tasks.</span>

## <a id="h.wtx4worlzz40" name="h.wtx4worlzz40"></a><span>Evaluation</span>

<span>Stage 1 is evaluated against the</span> <span class="c4">[following rubric](https://www.google.com/url?q=https://docs.google.com/document/d/1Q7b2dIt18r2pXqDhE_TsgZ5UkfzbybkGlmKfyoqpegc/pub?embedded%3Dtrue&sa=D&usg=AFQjCNF1sa7WrckpcPzBYDtcIlqJ1fg7jQ)</span><span>.</span>

<span></span>

# <a id="h.uckrb0yv2r11" name="h.uckrb0yv2r11"></a><span>User Experience Design</span>

<span></span>

<span>This guide will provide UI mock-ups (or mocks) to establish a baseline for Spotify Streamer</span><span>’s</span><span> core user experience. Your job is to implement these same experiences, although you’re free to customize the visual design to make it your own.</span>

<span></span>

<span>Note: In a real world situation, such mocks are generally provided by a designer or design team. But, in some cases, an individual developer creates their own mocks.</span>

<span></span>

<span>The main purpose of these mocks is to communicate and establish the visual and interactive experience of the entire app. The first two screens will be implemented in Stage 1, and the track player will be implemented in Stage 2.</span>

* * *

## <a id="h.5ma9q5opbn7g" name="h.5ma9q5opbn7g"></a><span>Phone Interaction Flow</span>

1.  <a id="h.l63g9tqyi1k" name="h.l63g9tqyi1k"></a><span class="c23">Search for an artist and display results</span>

<span></span>

<span style="overflow: hidden; display: inline-block; margin: 0.00px 0.00px; border: 0.00px solid #000000; transform: rotate(0.00rad) translateZ(0px); -webkit-transform: rotate(0.00rad) translateZ(0px); width: 300.00px; height: 533.00px;">![P1_Spotify_screen1_search_for_artist_300x533.png](https://lh3.googleusercontent.com/-5TJnw2Yg8CNInTW13kzAYOupdPLJT4a4nh4_AQLu1EJb6QeAU20-7FMRwx8Xg01S2sGLjGmITMtuJNXYOEyk7w09_jeQqnCo79jor3ePhFs55jS3pnQH2K8zBTiVuRG51m7Lo8)</span>

<span></span>

<span> 2\. Then display the top tracks for a selected artist.</span>

<span></span>

<span style="overflow: hidden; display: inline-block; margin: 0.00px 0.00px; border: 0.00px solid #000000; transform: rotate(0.00rad) translateZ(0px); -webkit-transform: rotate(0.00rad) translateZ(0px); width: 302.00px; height: 535.00px;">![](https://docs.google.com/drawings/image?id=sjxcYFKoqZ-jyPeVlMQiZYA&rev=1&h=535&w=302&ac=1)</span>

<span></span>

<span>3\. When a user selects a track, the track should start playing in a player UI (To be implemented in Stage 2)</span>

<span></span>

<span style="overflow: hidden; display: inline-block; margin: 0.00px 0.00px; border: 0.00px solid #000000; transform: rotate(0.00rad) translateZ(0px); -webkit-transform: rotate(0.00rad) translateZ(0px); width: 302.00px; height: 535.00px;">![](https://docs.google.com/drawings/image?id=spmvxzQZV1oLxf6jf17fYKA&rev=1&h=535&w=302&ac=1)</span>

* * *

<a id="h.h4j9w0p9dfnq" name="h.h4j9w0p9dfnq"></a>

# <a id="h.p69hpyxp4b00" name="h.p69hpyxp4b00"></a><span>Stage 2: Overview</span>

<span></span>

<span>This document should be used as a guide and development plan for Stage 2 of the Spotify Streamer app. It breaks down the build process into smaller, concrete milestones, and provides technical guidance on specific tasks.</span>

<span></span>

<span class="c12">Students are expected to have successfully completed Stage 1 prior to beginning Stage 2.</span>

<span></span>

<span>Supporting course material for this project</span><span>:</span> <span class="c4">[Developing Android Apps](https://www.google.com/url?q=https://www.udacity.com/course/ud853&sa=D&usg=AFQjCNE8GrTITuLkaQ5ZuyvstJsgPme5pw)</span><span>, Lessons 4-6.</span>

<span></span>

## <a id="h.lz10ve2ge20h" name="h.lz10ve2ge20h"></a><span>Evaluation</span>

<span>Stage 2 is evaluated against</span> <span class="c4">[this rubric](https://www.google.com/url?q=https://docs.google.com/document/d/1WAcuzWociiTXFBcV3Rx216ALL6TYUXwtfXOZzcd0TSU/pub?embedded%3Dtrue&sa=D&usg=AFQjCNGUKZuDNPQLVp8s8Y8g-cyH-Fb9RA)</span><span>.</span>

<span></span>

# <a id="h.nuqth7wyldbp" name="h.nuqth7wyldbp"></a><span>User Experience Design</span>

## <a id="h.qs1f143osbbc" name="h.qs1f143osbbc"></a><span>Tablet Interaction Flow</span>

<span>(using a Master Detail Flow)</span>

<span></span>

1.  <span>Search for an artist.</span>

<span style="overflow: hidden; display: inline-block; margin: 0.00px 0.00px; border: 0.00px solid #000000; transform: rotate(0.00rad) translateZ(0px); -webkit-transform: rotate(0.00rad) translateZ(0px); width: 538.50px; height: 401.95px;">![Spotify_Streamer_stage2_1.png](https://lh6.googleusercontent.com/Bd2K3L4XnnmoAtcNrgJItWW9V0sRe4y6vTcHGbVetbAF7KDAQate6qJfi5AVCpzU7o5aMGgXcIhWa3owg11kg6oSWYe4aZ31IARcDOxIb_BFaJ2t_wONTVj6Sx7w3eT8NyVp5So)</span>

<span></span>

<span>2\. Fetch and display the top tracks for the selected artist.</span>

<span></span>

<span style="overflow: hidden; display: inline-block; margin: 0.00px 0.00px; border: 0.00px solid #000000; transform: rotate(0.00rad) translateZ(0px); -webkit-transform: rotate(0.00rad) translateZ(0px); width: 532.50px; height: 398.54px;">![Spotify_Streamer_stage2_2.png](https://lh5.googleusercontent.com/uLmH5Yo83dtvkMaYPCK7_KxiB_HTWIa87kmVEOwTyzPwDaB2PaokcXy1HzF9iEtWqDLjuaM91VsUlh1jThjWKuuJtMb1eeebF0CALghAyBkdyWovK89jVIIeWrXHZWh9JjjH8y8)</span>

<span></span>

<span>3\. Play the selected track preview.</span>

<span></span>

<span style="overflow: hidden; display: inline-block; margin: 0.00px 0.00px; border: 0.00px solid #000000; transform: rotate(0.00rad) translateZ(0px); -webkit-transform: rotate(0.00rad) translateZ(0px); width: 405.50px; height: 303.35px;">![Spotify_Streamer_stage2_3.png](https://lh4.googleusercontent.com/m43RyQXZ4xYpMcncaosV5g8R_sM1sy6Gd632zqat69OWFLeWpoyvIZtb6lX-m5lw5EecFHinj1usgtbBCx3B9bxJ13PfFMqvzocT9mhp0rwKPEELWQtPXcv04qpO-azCUC-z4Pw)</span><span> </span><span style="overflow: hidden; display: inline-block; margin: 0.00px 0.00px; border: 0.00px solid #000000; transform: rotate(0.00rad) translateZ(0px); -webkit-transform: rotate(0.00rad) translateZ(0px); width: 251.22px; height: 335.50px;">![Spotify_Streamer_stage2_portrait.png](https://lh5.googleusercontent.com/Agqpx4bhJuR6C9uZoVM_jBpAgZ0kF3eHw7-wTNKhuVvvf3nFyDCRq-_aq7UmYo0aKX8wBHh9UKvCtVHuUzR3AiB7upyIFwQm6RdEyMgUO3TiWA19Mxz6R3i05pN7J1ke9xbgmps)</span>

<span></span>

</div>
