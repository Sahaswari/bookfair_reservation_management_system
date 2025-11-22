import { useState } from "react";
import { useNavigate } from "react-router-dom";
import { Button } from "@/components/ui/button";
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card";
import { Badge } from "@/components/ui/badge";
import { Checkbox } from "@/components/ui/checkbox";
import { Info, X } from "lucide-react";
import Header from "@/components/Header";
import { mockStalls, type Stall, type StallSize } from "@/data/mockData";
import { toast } from "sonner";

export default function Reserve() {
  const navigate = useNavigate();
  const [stalls, setStalls] = useState(mockStalls);
  const [selectedStalls, setSelectedStalls] = useState<string[]>([]);
  const [filterSize, setFilterSize] = useState<StallSize | "all">("all");
  const [showConfirmModal, setShowConfirmModal] = useState(false);

  const maxSelection = 3;
  const sizeColors = {
    small: "bg-blue-100 text-blue-700 dark:bg-blue-900/30 dark:text-blue-300",
    medium: "bg-amber-100 text-amber-700 dark:bg-amber-900/30 dark:text-amber-300",
    large: "bg-purple-100 text-purple-700 dark:bg-purple-900/30 dark:text-purple-300",
  };

  const handleStallClick = (stallId: string) => {
    const stall = stalls.find(s => s.id === stallId);
    if (!stall || stall.status === 'reserved') return;

    if (selectedStalls.includes(stallId)) {
      setSelectedStalls(selectedStalls.filter(id => id !== stallId));
      setStalls(stalls.map(s => s.id === stallId ? { ...s, status: 'available' } : s));
    } else {
      if (selectedStalls.length >= maxSelection) {
        toast.error(`Maximum ${maxSelection} stalls can be selected`);
        return;
      }
      setSelectedStalls([...selectedStalls, stallId]);
      setStalls(stalls.map(s => s.id === stallId ? { ...s, status: 'selected' } : s));
    }
  };

  const filteredStalls = filterSize === "all" 
    ? stalls 
    : stalls.filter(s => s.size === filterSize);

  const selectedStallsData = stalls.filter(s => selectedStalls.includes(s.id));
  const totalPrice = selectedStallsData.reduce((sum, s) => sum + s.price, 0);

  const handleReserve = () => {
    if (selectedStalls.length === 0) {
      toast.error("Please select at least one stall");
      return;
    }
    setShowConfirmModal(true);
  };

  const confirmReservation = () => {
    toast.success("Reservation successful!");
    navigate("/success", { state: { stalls: selectedStallsData, totalPrice } });
  };

  return (
    <div className="min-h-screen bg-background">
      <Header />
      
      <div className="container py-8">
        <div className="max-w-7xl mx-auto space-y-6">
          <div>
            <h1 className="text-4xl font-bold mb-2">Reserve Your Stall</h1>
            <p className="text-muted-foreground">
              Select up to {maxSelection} stalls from the exhibition hall map
            </p>
          </div>

          {/* Filters and Legend */}
          <Card>
            <CardContent className="pt-6">
              <div className="flex flex-wrap items-center justify-between gap-4">
                <div className="flex items-center gap-2">
                  <span className="text-sm font-medium">Filter by size:</span>
                  <Button 
                    variant={filterSize === "all" ? "default" : "outline"} 
                    size="sm"
                    onClick={() => setFilterSize("all")}
                  >
                    All
                  </Button>
                  <Button 
                    variant={filterSize === "small" ? "default" : "outline"} 
                    size="sm"
                    onClick={() => setFilterSize("small")}
                  >
                    Small
                  </Button>
                  <Button 
                    variant={filterSize === "medium" ? "default" : "outline"} 
                    size="sm"
                    onClick={() => setFilterSize("medium")}
                  >
                    Medium
                  </Button>
                  <Button 
                    variant={filterSize === "large" ? "default" : "outline"} 
                    size="sm"
                    onClick={() => setFilterSize("large")}
                  >
                    Large
                  </Button>
                </div>

                <div className="flex items-center gap-4 text-sm">
                  <div className="flex items-center gap-2">
                    <div className="w-4 h-4 rounded bg-primary"></div>
                    <span>Selected</span>
                  </div>
                  <div className="flex items-center gap-2">
                    <div className="w-4 h-4 rounded bg-card border-2"></div>
                    <span>Available</span>
                  </div>
                  <div className="flex items-center gap-2">
                    <div className="w-4 h-4 rounded bg-muted"></div>
                    <span>Reserved</span>
                  </div>
                </div>
              </div>
            </CardContent>
          </Card>

          <div className="grid lg:grid-cols-3 gap-6">
            {/* Stall Map */}
            <div className="lg:col-span-2">
              <Card>
                <CardHeader>
                  <div className="flex items-center justify-between">
                    <CardTitle>Exhibition Hall</CardTitle>
                    <Button variant="ghost" size="sm">
                      <Info className="h-4 w-4 mr-1" />
                      Guidelines
                    </Button>
                  </div>
                  <CardDescription>Click on available stalls to select them</CardDescription>
                </CardHeader>
                <CardContent>
                  <div className="grid grid-cols-8 gap-2">
                    {filteredStalls.map((stall) => (
                      <button
                        key={stall.id}
                        onClick={() => handleStallClick(stall.id)}
                        disabled={stall.status === 'reserved'}
                        className={`
                          aspect-square rounded-lg border-2 p-1 text-xs font-medium transition-all
                          ${stall.status === 'selected' ? 'bg-primary text-primary-foreground border-primary scale-105' : ''}
                          ${stall.status === 'available' ? 'bg-card hover:bg-accent hover:scale-105 cursor-pointer' : ''}
                          ${stall.status === 'reserved' ? 'bg-muted text-muted-foreground cursor-not-allowed opacity-50' : ''}
                        `}
                        title={`${stall.label} - ${stall.size} - LKR ${stall.price.toLocaleString()}`}
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

            {/* Selection Summary */}
            <div className="space-y-4">
              <Card>
                <CardHeader>
                  <CardTitle>Your Selection</CardTitle>
                  <CardDescription>
                    {selectedStalls.length} / {maxSelection} stalls selected
                  </CardDescription>
                </CardHeader>
                <CardContent className="space-y-4">
                  {selectedStallsData.length === 0 ? (
                    <p className="text-sm text-muted-foreground text-center py-4">
                      No stalls selected yet
                    </p>
                  ) : (
                    <>
                      <div className="space-y-2">
                        {selectedStallsData.map((stall) => (
                          <div key={stall.id} className="flex items-center justify-between p-2 bg-muted rounded-lg">
                            <div>
                              <p className="font-semibold">{stall.label}</p>
                              <p className="text-xs text-muted-foreground capitalize">{stall.size}</p>
                            </div>
                            <div className="flex items-center gap-2">
                              <span className="text-sm font-medium">
                                LKR {stall.price.toLocaleString()}
                              </span>
                              <Button
                                variant="ghost"
                                size="icon"
                                className="h-6 w-6"
                                onClick={() => handleStallClick(stall.id)}
                              >
                                <X className="h-4 w-4" />
                              </Button>
                            </div>
                          </div>
                        ))}
                      </div>

                      <div className="pt-4 border-t space-y-2">
                        <div className="flex justify-between text-lg font-bold">
                          <span>Total</span>
                          <span>LKR {totalPrice.toLocaleString()}</span>
                        </div>
                        <Button 
                          variant="hero" 
                          className="w-full" 
                          size="lg"
                          onClick={handleReserve}
                        >
                          Reserve Now
                        </Button>
                      </div>
                    </>
                  )}
                </CardContent>
              </Card>

              {/* Pricing Info */}
              <Card>
                <CardHeader>
                  <CardTitle className="text-base">Stall Pricing</CardTitle>
                </CardHeader>
                <CardContent className="space-y-2 text-sm">
                  <div className="flex justify-between">
                    <span>Small (3m × 2m)</span>
                    <span className="font-semibold">LKR 15,000</span>
                  </div>
                  <div className="flex justify-between">
                    <span>Medium (4m × 3m)</span>
                    <span className="font-semibold">LKR 25,000</span>
                  </div>
                  <div className="flex justify-between">
                    <span>Large (6m × 4m)</span>
                    <span className="font-semibold">LKR 40,000</span>
                  </div>
                </CardContent>
              </Card>
            </div>
          </div>
        </div>
      </div>

      {/* Confirmation Modal */}
      {showConfirmModal && (
        <div className="fixed inset-0 z-50 bg-background/80 backdrop-blur-sm flex items-center justify-center p-4">
          <Card className="w-full max-w-md">
            <CardHeader>
              <CardTitle>Confirm Reservation</CardTitle>
              <CardDescription>Please review your selection</CardDescription>
            </CardHeader>
            <CardContent className="space-y-4">
              <div className="space-y-2">
                {selectedStallsData.map((stall) => (
                  <div key={stall.id} className="flex justify-between text-sm">
                    <span>{stall.label} ({stall.size})</span>
                    <span>LKR {stall.price.toLocaleString()}</span>
                  </div>
                ))}
              </div>
              <div className="pt-2 border-t">
                <div className="flex justify-between font-bold">
                  <span>Total</span>
                  <span>LKR {totalPrice.toLocaleString()}</span>
                </div>
              </div>
              
              <div className="flex items-start space-x-2 pt-2">
                <Checkbox id="confirm-terms" defaultChecked />
                <label htmlFor="confirm-terms" className="text-sm text-muted-foreground leading-tight">
                  I accept the reservation terms and understand that payment must be completed within 7 days
                </label>
              </div>

              <div className="flex gap-2 pt-2">
                <Button variant="outline" className="flex-1" onClick={() => setShowConfirmModal(false)}>
                  Cancel
                </Button>
                <Button variant="hero" className="flex-1" onClick={confirmReservation}>
                  Confirm Reservation
                </Button>
              </div>
            </CardContent>
          </Card>
        </div>
      )}
    </div>
  );
}
