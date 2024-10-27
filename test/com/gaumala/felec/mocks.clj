(ns com.gaumala.felec.mocks)

(def mocked-bytes
  ;; 1500 random bytes
  (byte-array [46  46  29 236  71  57 183  12 192  90  78 207  86 109  20  41
               74 137 131  27 111  38 128  92 242 222 217 102   4 152 210 197
               7 225  61 151 216 111 156 164 132  92  34 245  93  68   3  96
               111 148   0 115  85 150  37   0 153  19  18 239  33 110 219  83
               88 219  95 165 104  22  38  16 237  60  90   4  76 225 170 158
               91 105 219  61 198 238 183 226 177  50 112 252  90 246  30  32
               86 190  98  40 132 248 150 149  30 235 230 116 149  64  36  26
               199 233 144  71  76 204 220  82  87 203 205  77 232 162 193  70
               88  99  83 197  21 208  21  30 211  80  96 249 137 235 174  94
               92 129 241 195 124  86 130 129 127   8  47 196 117 151   5 146
               34  67 242 100 184 177  78  38 108  20  56  69 209 106 184  13
               196 200 252 115 102 127 145 239 249  97  66 232 213  12 134  34
               90  18  70  94  18 124 141 249 237 141 214 160  99  66 209 204
               206 186  53  57 232 139 121  92 239 141  61 222 179 158 219  49
               16 100 209 237 240  56 239 214  21 231 171 105  42  35 121  31
               105  12 230  84 117  81 127  16 223  30 110  23 250 224 253 219
               49  45   6 200 184 197  87 113  36 165  82 189  81   9  48  16
               147 216  73 118 236 244 136 103 190 179 108 202  78  91 133  98
               133  86  15   6 161 130   8 181 145  94 253 177  99 114 207  27
               8 149  21 186  92 143 162  93  66  47 117  98  15  94  32   1
               252  26  46 124 188  37  54 142 121  36  18  29 124  56  48 175
               58 177 175 110 140  69  34 234 225  31   4 165  13 148 172 148
               211 254 217 238  51 170  86 162  73 220  43 243 102 132 120 105
               136  55   9 127 137 109 185 139 116  21 218 102 185 123  64 248
               43 133 226 198 153  99 228 110 150  11  99   1 220  57 176 247
               92 153  48 106  49 125 217 126 176 132 155 116  27 186 158  98
               130 247  34   4   5 237 127  65  54  90 167 202 223  86  74  95
               194  89 192  65 218  53 172 171 207 239 195  56 124 191 241  94
               22 249 160  11 182  83 176 234 180 106 146 222 226  33  48 214
               41  97 165 230  91 196  57 219 214 105  32 128 204  72  89 236
               54   9 153   2 105 237 180  90 120 158  44  38 164 202   1  54
               254  66  17   1  86  98 179 205  58  74 129 143 162 211 149 119
               173 154 158 251 101  74 161 103  17  75  14  75  47  72  39   5
               219 205 128  65  16  39 248 229  44  42  13  79 136 170 130   0
               238 132 142 153  57 125 231 131 232 126  71 116 184  54 208 252
               101 127  21 114  42 255 252 131 186   3 127  26 218 171 130 129
               110  26  64  69  73 173  61 166 136 194 155 127 215  26  37 133
               226  15  85 164  51 221 118  32 252 168 134  79 185  74 167  55
               233 145  88  43 201  70 123 220  93 100 122 193   2 251 144 234
               25 184  45 174  59 174 103  75  50 207  92  76  56  12  91  22
               227 144  65  61 205 235 128 120 109 120 112 104  87  69  11 119
               241 222 174 161 214   0  67 145 244 146 144   3  18  10 181  79
               253 149 176 174  13  95 137 171 153 248 140 179  18  18 128 133
               241  92 162 113 162  38 115  30  81 158  21 153  10  87 236  84
               248 113 211 206  64  76 227 153  63 185 231  29  24 248 185  38
               194 255  62 199   7  73  26 202  81  49  80 165 240  93 226 209
               182 108 152  83 164  89 115 137 115  55 233 203 173  72 162  61
               138  73 166 165  70  65 225  51  26 110 168   0 214 185  67 184
               93 108 184 235 115 205 151 134 168 229 124  46 108 109 238  66
               242  38 223  43 240  28   8 192 191 248  37  12 141  18 107 164
               226 242  11  49 105  64 206 223 120 117  15 254 214 211 244 105
               213  99 200 131 193 249  87 133 116  75 196 190 123  30  78 116
               109  59  49 166  40 220 241  14 252  79  23  29  29 144  76 131
               212 226  46  55 228 212  56 117  66 111 164  19 155  76  45 105
               236   5 221 241 101 153  94 169 200 113 154 111 111 112 131 181
               236 149 196 100 157 232 254 113  14 220 130  70  17 235  36 174
               29 208 241  87 198 214 177 185 193  82 154 205 248  40 255 200
               115 103  60  27 174  34 106  34   4 128 102 238 174  43 111  26
               131 183 110 139 230   8  34 140  87 225 134 118 202 111  77  11
               213  60 182 241 250  47 209  67  96 196 154 146  75 216 114  18
               137  83  56  27 207  63 250  26   6  67 215 216  59   8  88  68
               227  92 196  90  81 255 127 211  82 194 255 251 161 155 196 139
               189 185  99 207  47  68 195 246  39 172  62  37 250  27 151 140
               93 163   9 113 222   7  49  56  72 121  25  44 215   3 230  44
               57 216  80 121  25 222  69 111 196 101  61 128  19  85 155  45
               123 233 170 191   0 217 133  62 126  29 103 100 137 114  88 183
               127 215 251  46 253  94  30 151  49  76 248  91  53  66 201  72
               254 160  96 153 247  65 123 235  62  79 220 225 210 209 197  32
               20  70 216 251  54  72 237  17 160  71 230   1 231  45 108 166
               75 171 234  15  67 213 115 193  30   0 102 250  83  68 254 253
               25 103  42 122 144 159 169  48 231  81 114  40  63  49  39  20
               52  96  53 182  10 105 174 199 239  87  87  86 226  38 244 223
               42  90 245 179  25 129  70  60 169 215  73  14 153 147  48  74
               99  37  72 233  22 143  64  99 250   1 252 158  73  39  49 104
               19 167 254 136 206  34  26  20 182 189  51 210 245  12 171 212
               234  74 148  91  56 107 112 125 139  15 203  62  83  77 137 175
               1  83  87 170  29 101 220 196  13  32  37 183 131 145  46 187
               14  40  64  92 163 107  86 248  67 177 108  36  88 153 172 170
               17 221 155  77  63 245 255   7  36 102 197  41 249 160 187 162
               147 171 192 138 129  51  80   8  43 206 103  14 113  11 211  30
               237  51 118  49   9 210 168 197 169  34 245 248  45 102 145 135
               233  72  67 238 104  60  28 150 102  73  62 185 238  23 173 218
               40 255  83 200   3 111 127   8 115 108  28 138 227 253 146  19
               25 231 192 228  78 112 193 180  20 150  57 246 135  87 190  41
               67 116 248  92  95 234 117 168 186 179  27 205 192 168  18  70
               78  74 189  21 246 215 197 218 189 233   8 192 224  52  33  18
               66 187 105  23  28  73  23 204 218  74 179 244 182 206  33 142
               29  40 150 120 129 223 100 207 155  40   4 222 170  85 105  14
               30 225  84  76  80  27 128 234 204 209  74  81  11  33 111 120
               144 219  63 156  73 186   8 176 185  23 150  26 162 153 206 194
               194 164   1 163   5  37  82  14  98 131 147 115 115 244  22  87
               150 241 234 194  33 127  61 228  13 122 225  11 235 149  93  76
               98   9 189  53 125 248 129  88 111  96 222  77 212 173 104   5
               113 173 108  50 229  80  48 103  58  78 145 153]))

(def mocked-factura-content
  {:infoTributaria {} :infoFactura {} :detalles []})

(def mocked-factura-xml
  "<factura id=\"comprobante\" version=\"1.0.0\">
	<infoTributaria></infoTributaria>
  <infoFactura></infoFactura>
  <detalles></detalles>
  <infoAdicional></infoAdicional>
  </factura>")

(def mocked-ctx {:ambiente 1})

