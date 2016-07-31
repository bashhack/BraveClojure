// Compiled by ClojureScript 1.9.89 {}
goog.provide('test.core');
goog.require('cljs.core');
cljs.core.enable_console_print_BANG_.call(null);
test.core.concert_audience = new cljs.core.PersistentArrayMap(null, 4, [(0),new cljs.core.PersistentArrayMap(null, 3, [new cljs.core.Keyword(null,"has-tattoos?","has-tattoos?",-2108813408),false,new cljs.core.Keyword(null,"plays-accordian?","plays-accordian?",-1690541235),false,new cljs.core.Keyword(null,"name","name",1843675177),"Angus Lars Anthrax"], null),(1),new cljs.core.PersistentArrayMap(null, 3, [new cljs.core.Keyword(null,"has-tattoos?","has-tattoos?",-2108813408),false,new cljs.core.Keyword(null,"plays-accordian?","plays-accordian?",-1690541235),false,new cljs.core.Keyword(null,"name","name",1843675177),"Judas Jon Johannson"], null),(2),new cljs.core.PersistentArrayMap(null, 3, [new cljs.core.Keyword(null,"has-tattoos?","has-tattoos?",-2108813408),true,new cljs.core.Keyword(null,"plays-accordian?","plays-accordian?",-1690541235),true,new cljs.core.Keyword(null,"name","name",1843675177),"Ernst Van Streuselmeyer"], null),(3),new cljs.core.PersistentArrayMap(null, 3, [new cljs.core.Keyword(null,"has-tattoos?","has-tattoos?",-2108813408),true,new cljs.core.Keyword(null,"plays-accordian?","plays-accordian?",-1690541235),false,new cljs.core.Keyword(null,"name","name",1843675177),"Margot Gunnarschmitt"], null)], null);
test.core.metal_fan_details = (function test$core$metal_fan_details(social_security_number){
return cljs.core.get.call(null,test.core.concert_audience,social_security_number);
});
test.core.polka_enthusiast_QMARK_ = (function test$core$polka_enthusiast_QMARK_(record){
var and__2969__auto__ = new cljs.core.Keyword(null,"has-tattoos?","has-tattoos?",-2108813408).cljs$core$IFn$_invoke$arity$1(record);
if(cljs.core.truth_(and__2969__auto__)){
var and__2969__auto____$1 = new cljs.core.Keyword(null,"plays-accordian?","plays-accordian?",-1690541235).cljs$core$IFn$_invoke$arity$1(record);
if(cljs.core.truth_(and__2969__auto____$1)){
return record;
} else {
return and__2969__auto____$1;
}
} else {
return and__2969__auto__;
}
});
test.core.identify_polka_enthusiast = (function test$core$identify_polka_enthusiast(social_security_numbers){
return cljs.core.first.call(null,cljs.core.filter.call(null,test.core.polka_enthusiast_QMARK_,cljs.core.map.call(null,test.core.metal_fan_details,social_security_numbers)));
});
cljs.core.println.call(null,(function (){var start__3429__auto__ = cljs.core.system_time.call(null);
var ret__3430__auto__ = test.core.identify_polka_enthusiast.call(null,cljs.core.range.call(null,(0),(1000000)));
cljs.core.prn.call(null,[cljs.core.str("Elapsed time: "),cljs.core.str((cljs.core.system_time.call(null) - start__3429__auto__).toFixed((6))),cljs.core.str(" msecs")].join(''));

return ret__3430__auto__;
})());

//# sourceMappingURL=core.js.map