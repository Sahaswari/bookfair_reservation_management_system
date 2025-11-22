import { useState } from "react";
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Badge } from "@/components/ui/badge";
import { Search, Info } from "lucide-react";
import Header from "@/components/Header";
import { mockStalls, type Stall } from "@/data/mockData";

export default function StallManagement() {
  const [stalls] = useState(mockStalls);
  const [selectedStall, setSelectedStall] = useState<Stall | null>(null);
  const [searchQuery, setSearchQuery] = useState("");

  const sizeColors = {
    small: "bg-blue-100 text-blue-700 dark:bg-blue-900/30 dark:text-blue-300",
    medium: "bg-amber-100 text-amber-700 dark:bg-amber-900/30 dark:text-amber-300",
    large: "bg-purple-100 text-purple-700 dark:bg-purple-900/30 dark:text-purple-300",
  };

  const handleStallClick = (stall: Stall) => {
    setSelectedStall(stall);
  };

  return (
    <div className="min-h-screen bg-background">
      <Header isEmployee />
      
      <div className="container py-8">
        <div className="space-y-6">
          <div className="flex items-center justify-between">
            <div>
              <h1 className="text-4xl font-bold mb-2">Stall Management</h1>
              <p className="text-muted-foreground">
                View and manage exhibition hall stalls
              </p>
            </div>
            <div className="relative w-64">
              <Search className="absolute left-3 top-3 h-4 w-4 text-muted-foreground" />
              <Input
                placeholder="Search by vendor or stall..."
                value={searchQuery}
                onChange={(e) => setSearchQuery(e.target.value)}
                className="pl-9"
              />
            </div>
          </div>

          <div className="grid lg:grid-cols-3 gap-6">
            {/* Map */}
            <div className="lg:col-span-2">
              <Card>
                <CardHeader>
                  <div className="flex items-center justify-between">
                    <CardTitle>Exhibition Hall</CardTitle>
                    <div className="flex items-center gap-4 text-sm">
                      <div className="flex items-center gap-2">
                        <div className="w-4 h-4 rounded bg-muted"></div>
                        <span>Reserved</span>
                      </div>
                      <div className="flex items-center gap-2">
                        <div className="w-4 h-4 rounded bg-card border-2"></div>
                        <span>Available</span>
                      </div>
                    </div>
                  </div>
                  <CardDescription>Click on any stall to view details</CardDescription>
                </CardHeader>
                <CardContent>
                  <div className="grid grid-cols-8 gap-2">
                    {stalls.map((stall) => (
                      <button
                        key={stall.id}
                        onClick={() => handleStallClick(stall)}
                        className={`
                          aspect-square rounded-lg border-2 p-1 text-xs font-medium transition-all cursor-pointer
                          ${stall.status === 'reserved' ? 'bg-muted border-muted' : 'bg-card hover:bg-accent'}
                          ${selectedStall?.id === stall.id ? 'ring-2 ring-employee scale-105' : ''}
                        `}
                      >
                        <div className="flex flex-col items-center justify-center h-full">
                          <span className="font-bold">{stall.label}</span>
                          <Badge variant="outline" className={`text-[8px] px-1 mt-1 ${sizeColors[stall.size]}`}>
                            {stall.size[0].toUpperCase()}
                          </Badge>
                        </div>
                      </button>
                    ))}
                  </div>
                </CardContent>
              </Card>
            </div>

            {/* Stall Details */}
            <div>
              <Card className="sticky top-4">
                {selectedStall ? (
                  <>
                    <CardHeader>
                      <div className="flex items-center justify-between">
                        <CardTitle>Stall {selectedStall.label}</CardTitle>
                        <Badge variant={selectedStall.status === 'reserved' ? 'secondary' : 'default'}>
                          {selectedStall.status}
                        </Badge>
                      </div>
                    </CardHeader>
                    <CardContent className="space-y-4">
                      <div className="space-y-2">
                        <div className="flex justify-between text-sm">
                          <span className="text-muted-foreground">Size</span>
                          <span className="font-medium capitalize">{selectedStall.size}</span>
                        </div>
                        <div className="flex justify-between text-sm">
                          <span className="text-muted-foreground">Price</span>
                          <span className="font-medium">LKR {selectedStall.price.toLocaleString()}</span>
                        </div>
                        <div className="flex justify-between text-sm">
                          <span className="text-muted-foreground">Location</span>
                          <span className="font-medium">Row {String.fromCharCode(65 + selectedStall.row)}, Col {selectedStall.col + 1}</span>
                        </div>
                      </div>

                      {selectedStall.status === 'reserved' && (
                        <>
                          <div className="pt-4 border-t space-y-2">
                            <p className="text-sm font-medium">Vendor Information</p>
                            <div className="space-y-1 text-sm">
                              <div className="flex justify-between">
                                <span className="text-muted-foreground">Business</span>
                                <span className="font-medium">Sample Publisher</span>
                              </div>
                              <div className="flex justify-between">
                                <span className="text-muted-foreground">Contact</span>
                                <span className="font-medium">John Doe</span>
                              </div>
                              <div className="flex justify-between">
                                <span className="text-muted-foreground">Email</span>
                                <span className="font-medium text-xs">vendor@example.com</span>
                              </div>
                            </div>
                          </div>

                          <Button variant="destructive" className="w-full" size="sm">
                            Release Stall
                          </Button>
                        </>
                      )}

                      {selectedStall.status === 'available' && (
                        <Button variant="employee" className="w-full">
                          Manually Reserve
                        </Button>
                      )}
                    </CardContent>
                  </>
                ) : (
                  <CardContent className="py-16 text-center">
                    <Info className="h-12 w-12 text-muted-foreground mx-auto mb-4" />
                    <p className="text-muted-foreground">
                      Select a stall from the map to view details
                    </p>
                  </CardContent>
                )}
              </Card>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}
