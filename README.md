# Tema

## Exercitiul 1
a) linia 64 - pred.next = node; : aici legaturile sunt complete si nodul este adaugat in lista

b) linia 60 - return false; : aici sau la verificarea conditiei din if stim ca adaugarea a esuat

c) linia 91 - pred.next = current.next; : aici deoarece dupa aceasta linie de cod nodul devine inaccesibil

d) linia 94 - return false; : aici sau la (ne)verificarea conditiei din if stim ca eliminarea a esuat


## Exercitiul 2
a) nu este necesara fiindca variabila este atomica

b) coada nu va mai functiona corect deoarece avand cel putin 3 elemente in coada putem realiza secvente de tipul enq -> deq -> enq pentru a adauga 2 noduri simultan(head va fi diferit dupa deq si vom avea 2 lock-uri la dispozitie) si in cazul in care al doilea enq se realizeaza mai repede se pierde propietatea de fifo

c)
- da, ar merge in continuare bine coada
- ar aparea probleme la mai multe operatii de deq

d) este necesar, daca nu ar fi in sectiunea protejata de lock mai multe thread-uri ar putea trece fara a vedea modificarile facute de celelalte thread-uri si s-ar incerca elmiminarea mai multor elemente decat numarul de elemente existente in coada

## Exercitiul 3